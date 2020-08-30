#kafka解决的问题#
- 解耦合
- 数据持久化
- 扩展与容灾
	- 容灾：kafka每个topic都可以分为多个partition(分区)，每个分区都有多个replica(副本)，实现消息冗余备份。每个分区中的消息都不同(类似于数据库中水平切分)，副本之间是一主多从的关系，其中，leader副本负责处理读写请求，follower副本则只与leader副本进行消息同步。
		在consumer端使用pull方式从服务端拉消息，并且在comsumer端保存消费的具体未知，当消费者宕机恢复之后，可以根据自己保存的消费位置重新拉取需要的消息进行消费。
	- 扩展：支持consumer的水平扩展能力。
- 顺序保证：kafka保证一个partition内消息的有序性，但是并不保证多个partition之间的数据有序。
- 异步通信。


#producer#
producer流程图：
![](/picture/kafka-producer-flow.png)

1. ProducerInterceptors对消息进行拦截
2. Serializer对消息的key和value进行序列化
3. Partitioner为消息选择合适的Partition
4. RecordAccumulator手机消息，实现批量发送
5. Sender从RecordAccumulator获取消息
6. 构造ClientRequest
7. 将ClientRequest交给NetworkClient，准备发送
8. NetworkClient将请求放入KafkaChannel的缓存
9. 执行网络I/O,发送请求
10. 收到响应，调用ClientRequest的回调函数。
11. 调用RecordBatch的回调函数，最终调用每个消息上注册的回调函数。

消息发送的过程中，设计两个线程协同工作。主线程首先将业务数据封装成ProducerRecord对象，之后调用send()方法将消息放入RecordAccumulator（消息收集器，即主线程与sender线程之间的缓冲区）中暂存。
Sender线程负责将消息信息构成请求，并最终执行网络I/O的线程，它从RecordAccumulator中取出消息并批量发送出去。
KafkaProducer是线程安全的，多个线程可以共享使用同一个KafkaProducer对象。

KafkaProducer发送流程:
![](/picture/KafkaProducer-send-flow.png)

1. 调用ProducerInterceptors.onSend()方法，通过ProducerInterceptor对消息进行拦截或修改
2. 调用waitOnMetadata()方法获取Kafka集群的信息，底层会幻想send线程更新Metadata中保存的kafka集群元数据
3. 调用Serializer.serialize()方法序列化消息的key和value
4. 调用partition()为消息选择合适的分区
5. 调用RecordAccumulator.append()方法，将消息追加到RecordAccumulator中
6. 唤醒sender线程，由sender线程将RecordAccumulator中缓存的消息发送出去

####ProducerInterceptor####
ProducerInterceptor对象可以在消息发送之前对其进行拦截或修改，也可以先于用户的Callback,对ACK响应进行预处理。


####kafka集群元数据####
- KafkaProducer
````
    /** clientId的生成器，如果没有明确指定client的Id,则使用字段生成一个ID */
    private static final AtomicInteger PRODUCER_CLIENT_ID_SEQUENCE = new AtomicInteger(1);
    private static final String JMX_PREFIX = "kafka.producer";
    /** 此生产者的唯一标识 */
    private String clientId;
    /** 分区选择器，根据一定的策略，将消息路由到合适的分区 */
    private final Partitioner partitioner;
    /** 消息的最大长度，这个长度包含了消息头，序列化后的key和序列化后的value的长度 */
    private final int maxRequestSize;
    /** 发送单个消息的缓冲区大小 */
    private final long totalMemorySize;
    /** 整个kafka集群的元数据 */
    private final Metadata metadata;
    /** RecordAccumulator,用于收集并缓存消息，等待sender发送 */
    private final RecordAccumulator accumulator;
    /** 发送消息的sender任务，实现Runnable接口，在ioThread线程中执行 */
    private final Sender sender;
    private final Metrics metrics;
    /** 执行sender任务发送消息的线程，称为“sender线程” */
    private final Thread ioThread;
    /** 压缩算法，可选项有：none.gzip.snappy.lz4。这是针对RecordAccumulator中多条消息进行的压缩，所以消息越多，压缩效果越好 */
    private final CompressionType compressionType;
    private final Sensor errors;
    private final Time time;
    /** key的序列化器 */
    private final Serializer<K> keySerializer;
    /** value的序列化器 */
    private final Serializer<V> valueSerializer;
    private final ProducerConfig producerConfig;
    /** 等待更新kafka集群元数据的最大时长 */
    private final long maxBlockTimeMs;
    /** 消息的超时时间，也就是从消息发送到收到ACK响应的最长时长 */
    private final int requestTimeoutMs;
    /** ProducerInterceptor集合，ProducerInterceptor可以在消息发送之前对其进行拦截或修改，也可以先于用户的Callback,对ACK响应进行预处理 */
    private final ProducerInterceptors<K, V> interceptors;
````
KafkaProducer.waitOnMetadata()函数：负责触发kafka集群元数据的更新，并阻塞主线程等待更新完毕。主要步骤是
1. 检测Metadata中是否包含指定topic的元数据，若不包含，则将topic添加到topics集合中，下次更新时会从服务端获取指定topic的元数据
2. 尝试获取topic中分区的详细信息，失败后会调用requestUpdate()方法设置Metadata.needUpdate字段，并得到当前元数据版本号
3. 唤醒sender线程，由sender线程更新Metadata中保存的kafka集群元数据
4. 主线程调用awaitUpdate()方法，等待sender线程完成更新
5. 从Metadata中获取指定topic分区的详细信息(PatitionInfo集合)。若失败，则回到步骤2继续尝试，若等待时间超时，则抛出异常。

- Cluster
````
    /** kafka集群中节点信息列表 */
    private final List<Node> nodes;
    private final Set<String> unauthorizedTopics;
    /** TopicPartition和PartitionInfo之间的映射关系 */
    private final Map<TopicPartition, PartitionInfo> partitionsByTopicPartition;
    /** topic和PartitionInfo映射，List<PartitionInfo>不一定有leader */
    private final Map<String, List<PartitionInfo>> partitionsByTopic;
    /** topic与PartitionInfo映射，List<PartitionInfo>中存放的分区必须是有leader副本的Partition */
    private final Map<String, List<PartitionInfo>> availablePartitionsByTopic;
    /** node和PartitionInfo映射，可以按照节点id查询其上分布的全部分区的详细信息 */
    private final Map<Integer, List<PartitionInfo>> partitionsByNode;
    /** brokerId和Node之间的对应关系 */
    private final Map<Integer, Node> nodesById;
````

- Metadata
````
    /** 两次发出更新cluster保存的元数据信息的最小时间差，默认为10ms。这是为了防止更新操作过于频繁而造成网络阻塞和增加服务端压力。 */
    private final long refreshBackoffMs;
    /** 每隔多久更新一次，默认300*1000(5min) */
    private final long metadataExpireMs;
    /** kafka集群元数据的版本号。kafka集群元数据每更新成功一次，version字段的值增1.通过新旧版本号的比较，判断集群元数据是否更新完成 */
    private int version;
    /** 记录上一次更新元数据的时间戳（也包含更新失败的情况） */
    private long lastRefreshMs;
    /** 上一次成功更新的时间戳。如果每次都成功，则 lastSuccessfulRefreshMs、lastRefreshMs相等，否则lastRefreshMs>lastSuccessfulRefreshMs */
    private long lastSuccessfulRefreshMs;
    /** 记录kafka集群的元数据 */
    private Cluster cluster;
    /** 标识是否强制更新cluster，这是触发sender线程更新集群元数据的条件之一 */
    private boolean needUpdate;
    /** 记录了当前已知的所有topic */
    private final Set<String> topics;
    /** 监听Metadata更新的监听器集合。 */
    private final List<Listener> listeners;
    /** 是否需要更新全部topic的元数据，一般情况下，KafkaProducer只维护它用到的topic的元数据，是集群中全部topic的子集 */
    private boolean needMetadataForAllTopics;
````

- Serializer&Deserializer
客户端发送的小时的key和value都是byte数组，Serializer和Deserializer接口提供了java对象序列化(反序列化)为byte数组的功能。

- Partitioner
在KafkaProducer.partition()方法中，优先根据ProducerRecord中partition字段指定的序列号选择分区，
如果PartitionRecord.partition字段没有明确指定分区编号，则通过Patitioner.partition()方法选择Partition

DefaultPartitioner.partition()方法负责在ProduceRecord中没有明确指定分区编号的时候，为其选择合适的分区；
如果消息没有key会根据counter与Partition个数取模来确定分区编号，counter不断递增，确保消息不会都发到同一个Partition里；
如果有key的话，则对key进行hash，然后与分区数量取模，来确定key所在的分区达到负载均衡

##RecordAccumulator##
KafkaProducer可以有同步和异步两种方式发送消息，其实两者的底层实现相同，都是通过异步方式实现的。主线程调用KafkaProducer.send()方法发送消息的时候，
先将消息放到RecordAccumulator中暂存，然后主线程就可以从send()方法中返回来，此时消息并没有真正的发送给kafka，而是缓存在了RecordAccumulator中。
之后业务线程通过KafkaProducer.send()方法不断向RecordAccumulator追加消息，当达到一定的条件时，会唤醒sender线程发送RecordAccumulator中的消息

**RecordAccumulator至少有一个业务线程和sender线程并发操作，所以必须是线程安全的**

RecordBatch拥有一个MemoryRecords对象的引用
MemoryRecords是存放消息的地方

####MemoryRecords####
MemoryRecords是多个消息的集合，其中封装了Java NIO ByteBuffer用来保存消息数据，
Compressor用于对ByteBuffer中的消息进行压缩，以及其他控制字段

````
    /** 压缩器，对消息数据进行压缩，将压缩后的数据输出到buffer*/
    private final Compressor compressor;
    /** 记录buffer字段最多可以写入多少个字节的数据*/
    private final int writeLimit;
    /** 用于保存消息数据的 Java NIO ByteBuffer */
    private ByteBuffer buffer;
    /** 此MemoryRecords对象是只读模式还是可写模式，在MemoryRecords发送钱，会将其设置成只读模式 */
    private boolean writable;
````

####RecordBatch####

````
    /** 记录了保存的Record的个数 */
    public int recordCount = 0;
    /** 最大Record的字节数 */
    public int maxRecordSize = 0;
    /** 尝试发送当前RecordBatch的次数 */
    public volatile int attempts = 0;
    /** 最后一次尝试发送的时间戳 */
    public long lastAttemptMs;
    /** 指向用来存储数据的MemoryRecords对象 */
    public final MemoryRecords records;
    /** 当前RecordBatch中缓存的消息都会发送给此TopicPartition */
    public final TopicPartition topicPartition;
    /** 标识RecordBatch状态的Future对象 */
    public final ProduceRequestResult produceFuture;
    /** 最后一次向RecordBatch追加消息的时间戳 */
    public long lastAppendTime;
    /** Thunk对象集合 */
    private final List<Thunk> thunks;
    /** 用来记录某消息在RecordBatch中的偏移量 */
    private long offsetCounter = 0L;
    /** 是否正在重试。如果RecordBatch中的数据发送失败，则会重新尝试发送 */
    private boolean retry;
````

当RecordBatch中全部的消息被正常响应、超时或关闭生产者时，会调用ProduceRequestResult.done()方法。将produceFuture标记为完成并通过ProduceRequestResult.error字段区分"异常完成"还是"正常完成"，
之后调用CountDownLatch对象的countDown()方法。此时会阻塞在CountDownLatch对象的await()方法的线程

tryAppend()函数是最核心的方法，其功能是将消息添加到当前的RecordBatch中缓存

####ProduceRequestResult####
自实现的一个类似Future功能的类


####BufferPool####

部分字段
````
    /** 记录整个pool的大小 */
    private final long totalMemory;
    /** 因为有多线程并发分配和回收ByteBuffer，所以使用锁控制并发，保证线程安全 */
    private final ReentrantLock lock;
    /** 一个Deque<ByteBuffer>队列，其中缓存了指定大小的ByteBuffer对象 */
    private final Deque<ByteBuffer> free;
    /** 记录因申请不到足够空间而阻塞的线程，此队列中实际记录的是阻塞线程对应的Condition对象 */
    private final Deque<Condition> waiters;
    /** 记录可用的空间大小，这个空间是totalMemory减去free表总全部ByteBuffer的大小 */
    private long availableMemory;
````

[kafka内存管理详细介绍](https://www.jianshu.com/p/2f4d56ee9315)

####RecordAccumulator####
部分字段
````
    /** 指定每个RecordBatch底层ByteBuffer的大小 */
    private final int batchSize;
    /** 压缩类型 */
    private final CompressionType compression;
    /** BufferPool对象 */
    private final BufferPool free;
    /** 未发送完成的RecordBatch集合，底层通过Set<RecordBatch>集合实现 */
    private final IncompleteRecordBatches incomplete;
    // The following variables are only accessed by the sender thread, so we don't need to protect them.
    private final Set<TopicPartition> muted;
    /** 使用drain方法批量导出RecordBatch时，为了防止饥饿，使用drainIndex记录上次发送停止时的位置，下次继续从此位置开始发送 */
    private int drainIndex;
````

append()将消息追加到RecordAccumulator中.步骤：
1. 首先在batches集合中查找TopicPartition对应的Deque，查找不到，则创建新的Deque，并添加到batches中
2. 对Deque加锁（synchronized关键字）
3. 调用tryAppend()方法，尝试向Deque中最后一个RecordBatch追加Record
4. synchronized块结束，自动解锁
5. 追加成功，则返回RecordAppendResult(其中封装了ProduceRequestResult)
6. 追加失败，尝试从BufferPool中申请新的ByteBuffer
7. 对Deque加锁（使用synchronized关键字），再次尝试3步
8. 追加成功，则返回；失败，则使用5步得到的ByteBuffer创建RecordBatch
9. 将Record追加到新建的RecordBatch中，并将新建的RecordBatch追加到对应的Deque尾部
10. 将新建的RecordBatch追加到incomplete集合
11. synchronized结束，自动解锁
12. 返回RecordAppendResult.RecordAppendResult中的字段会作为唤醒sender线程的条件

##sender分析##
sender线程发送消息的整个流程是：
1. 首先根据RecordAccumulator的缓存情况，筛选出可以向那些Node节点发送消息，即RecordAccumulator.ready()函数
2. 然后根据生产设与各个节点的连接情况(由NetworkClient管理)，过滤Node节点；
3. 之后生成相应的请求(每个Nod节点只生成一个请求)
4. 最后调用NetworkClient将请求发送出去

sender.run()函数时序图
![](/picture/sender-run.png)

Sender.run(long)函数流程：
1. 从Metadata获取kafka集群元数据
2. 调用RecordAccumulator.ready()函数，根据RecordAccumulator的缓存情况，选择可以向那些Node节点发送消息，返回ReadyCheckResult对象
3. 如果ReadyCheckResult中表示有unknownLeadersExist,则调用Metadata的requestUpdata函数，标记需要更新kafka的集群信息
4. 针对ReadyCheckResult中readyNodes集合，循环调用NetworkClient.ready()函数，目的是检查网络I/O方面是否符合发送消息的条件，不符合条件的Node将会从readyNodes集合中删除
5. 针对经过步骤4处理后的readyNodes集合，调用RecordAccumulator.drain()函数，获取等待发送晓得消息集合
6. 调用RecordAccumulator.abortExpiredBatches()函数，处理RecordAccumulator中超时的消息。（逻辑是遍历RecordAccumulator中保存的全部RecordBatch，调用RecordBatch.maybeExpire()方法进行处理。如果已超时，则调用RecordBatch.done()函数，其中会触发自定义Callback.并将RecordBatch从队列中移除，释放ByteBuffer空间）
7. 调用Sender.createProduceRequests()方法将待发送的消息封装成ClientRequest
8. 调用NetWorkClient.send()函数，将ClientRequest写入KafkaChannel的send字段
9. 调用NetWorkClient.poll()函数，将KafkaChannel.send字段中保存的ClientRequest发送出去，同时，还会处理服务端发回的响应、处理超时的请求、调用用户自定义Callback等等



生产者请求响应数据格式：
![](/picture/producer-request-response.png)


####Selector####

部分字段：
````
    /** java.nio.channels.Selector类型，用来监听网络I/O事件 */
    private final java.nio.channels.Selector nioSelector;
    /**  Map<String, KafkaChannel>类型，维护了NodeId与KafkaChannel之间的映射关系，表示生产者客户端与各个node之间的网络连接。 */
    private final Map<String, KafkaChannel> channels;
    /** 记录已经完全发送出去的请求 */
    private final List<Send> completedSends;
    /** 记录已经完全接收到的请求 */
    private final List<NetworkReceive> completedReceives;
    /** 暂存一次OP_READ事件处理过程中读取到的全部请求。当一次OP_READ事件处理完成之后，会将stagedReceives集合中的请求保存到completedReceives集合中 */
    private final Map<KafkaChannel, Deque<NetworkReceive>> stagedReceives;
    /** 记录一次poll过程中发现的断开的连接 */
    private final List<String> disconnected;
    /** 记录一次poll过程中发现的新建立的连接 */
    private final List<String> connected;
    /** 记录向哪些node发送的请求失败了 */
    private final List<String> failedSends;
    /** 用于创建KafkaChannel的builder。根据不同配置创建不同的TransportLayer的子类，然后创建KafkaChannel */
    private final ChannelBuilder channelBuilder;
    /** 用来记录各个连接的使用情况，并据此关闭空闲时间超过connectionsMaxIdleNanos的连接 */
    private final Map<String, Long> lruConnections;
````

####org.apache.kafka.common.network.Selector####
使用NIO异步非阻塞模式实现网络I/O操作，使用一个单独的线程可以管理多条网络连接上的连接、读、写等操作。

部分字段：
````
    /** java.nio.channels.Selector类型，用来监听网络I/O事件 */
    private final java.nio.channels.Selector nioSelector;
    /**  Map<String, KafkaChannel>类型，维护了NodeId与KafkaChannel之间的映射关系，表示生产者客户端与各个node之间的网络连接。 */
    private final Map<String, KafkaChannel> channels;
    /** 记录已经完全发送出去的请求 */
    private final List<Send> completedSends;
    /** 记录已经完全接收到的请求 */
    private final List<NetworkReceive> completedReceives;
    /** 暂存一次OP_READ事件处理过程中读取到的全部请求。当一次OP_READ事件处理完成之后，会将stagedReceives集合中的请求保存到completedReceives集合中 */
    private final Map<KafkaChannel, Deque<NetworkReceive>> stagedReceives;
    /** 记录一次poll过程中发现的断开的连接 */
    private final List<String> disconnected;
    /** 记录一次poll过程中发现的新建立的连接 */
    private final List<String> connected;
    /** 记录向哪些node发送的请求失败了 */
    private final List<String> failedSends;
    /** 用于创建KafkaChannel的builder。根据不同配置创建不同的TransportLayer的子类，然后创建KafkaChannel */
    private final ChannelBuilder channelBuilder;
    /** 用来记录各个连接的使用情况，并据此关闭空闲时间超过connectionsMaxIdleNanos的连接 */
    private final Map<String, Long> lruConnections;
````

####KafkaChannel####
KafkaChannel负责基于socket的连接，认证，数据读取发送。它包含TransportLayer和Authenticator两个部分。TransportLayer负责数据交互，Authenticator负责安全验证。


####InFlightRequests####
主要作用是缓存已经发出去但是没有收到响应的ClientRequest。底层是通过一个Map<String, Deque<ClientRequest>>对象实现，key是NodeId,value是发送到对应Node的ClientRequest对象集合

####MetadataUpdater####
辅助NetworkClient更新的Metadata的接口。

返回数据格式
![](/picture/metadata-response.png)

####NetworkClient####
NetworkClient中左右连接的状态都由ClusterConnectionStates管理，他底层使用Map<String, NodeConnectionState>实现

此类是一个通过网络客户端实现，不只用于生产者发送消息，也可以用于消费者消费消息以及服务端broker之间的通信

ready()函数用来检查Node是否准备好接受数据。首先通过 isReady()函数检查是否可以向一个Node发送请求，需要符合以下三个条件，表示Node已经准备好
1. Metadata并未处于正在更新或需要更新的状态。
2. 已成功建立连接并且连接正常，connectionStates.isConnected(node)
3. InFlightRequests.canSendMore()返回true

如果NetworkClient.isReady()返回false,且满足以下两个条件，则会调用initiateConnect()方法发起连接
1. 连接不能是CONNECTING状态，必须是DISCONNECTED
2. 为了避免网络拥塞，重连不能太频繁，两次重试之间的时间差必须大于重试的退避时间，由reconnectBackoffMs字段指定


NetworkClient.initiateConnect()方法会修改ClusterConnectionStates中的连接状态，并调用Selector.connect()方法发起连接。之后调用Selector.pollSelectionKeys()方法时，判断连接是否建立。
如果建立成功，则将ConnectionState设置为CONNECTED.

NetworkClient.send()函数主要是将请求设置到KafkaChannel.send字段，同时将请求添加到InFlightRequests队列中等待响应


#消费者#
kafka服务端不会记录消费者的消费位置，而是由消费者自己决定如何保存和记录其消费的offset。在新版中，在kafka服务端中添加了一个名为“_consumer_offsets”的内部topic，用来保存消费者提交的offset，
当出现消费者上/下线时，会触发consumer group 进行rebalance操作，对分区进行重新分配，待rebalance操作完成后，消费者就可以读取“_consumer_offsets”中记录的offset，并从此offset位置继续消费。

##传递保证##
三个级别
1. at most once:消息可能会丢，但绝不会重复
2. at least once:消息绝不会丢，但可能会重复传递
3. exactly once:每条消息只会被传递一次。由生产者和消费者两部分共同决定：首先，生产者要保证不会产生重复的消息；其次消费者不能重复拉取相同的消息。

“exactly once”可以有两种可选方案：
1. 每个分区只有一个生产者写入消息，当出现异常或超时的情况时，生产者就要查询此分区的最后一个消息，用来决定后续操作是消息重传还是继续发送
2. 为每个消息添加一个全局唯一主键，生产者不做其他特殊处理，按照重传方式进行重传，由消费者对消息进行去重，实现“exactly once”定义

消费者处理消息的与提交offset的顺序，很大程度上决定了消费者是哪个定义。

##consumer group rebalance##

版本对比：
####方案一####
通过zookeeper的watcher实现。每个consumer group在zookeeper下都维护一个"/consumer/[group_id]/ids"路径，在此路径下使用临时节点记录属于此consumer group的消费者的id，由consumer启动时创建。
还有两个同级节点:
owners节点：记录了分区及消费者的对应关系
offsets节点：记录consumer group在某个分区上的消费位置
每个broker.topic以及分区在zookeeper中也都对应一个路径

每个consumer分别在"/consumer/[group_id]/ids" 和"/brokers/ids"路径上注册一个watcher。
当"/consumer/[group_id]/ids" 路径的子节点发生变化时，表示consumer group中的消费者出现了变化；
当"/brokers/ids"路径的子节点发生变化时，表示broker出现了增减。
这样，通过watcher，每个消费者就可以监控consumer group和kafka集群的状态

问题：
1. 羊群效应：一个被watch的zookeeper节点变化，导致大量的watcher通知需要被发送给客户端，这将导致在通知期间其他操作延迟。
	任何broker或consumer加入或退出，都会向其余所有的consumer发送watcher通知触发rebalance
2. 脑裂：每个consumer都是通过zookeeper中保存的这些元数据判断consumer group状态，broker的状态，以及rebalance结果。
	由于zookeeper只能保证“最终一致性”，不保证“simultaneously consistent cross-client views”(数据同时一致可见性)，不同consumer在同一时刻可能连接到zookeeper不同的服务器，看到的元数据可能不一样，就会造成不正确的rebalance尝试
	
####方案二####
将全部的consumer group 分成多个子集，每个consumer group 子集在服务端对应一个 GroupCoordinator对其进行管理。
GroupCoordinator是kafkaServer中用于管理consumer group的组件，。消费者不再依赖zookeeper，而只有GroupCoordinator在zookeeper上添加watcher。
消费者在加入或退出consumer group时会修改zookeeper中保存的元数据，此时会触发GroupCoordinator设置的watcher，通知GroupCoordinator开始rebalance操作。

1. 当前消费之准备加入consumer group或是GroupCoordinator发生故障转移时，消费者并不知道GroupCoordinator的网络位置，消费者会向kafka集群中的任一broker发送ConsumerMetadataRequest，此请求包含了其 consumer group 的groupId，
	收到请求的broker会返回ConsumerMetadataResponse作为响应，其中包含了管理此Consumer group的GroupCoordinator的相关信息
2. 消费者根据ConsumerMetadataResponse中的GroupCoordinator信息，连接到GroupCoordinator并周期性的发送HeadbeatReqeust.发送HeadbeatReqeust主要作用是为了告诉GroupCoordinator此消费者正常在线，
    GroupCoordinator会认为长时间未发送HeadbeatReqeust的消费者已经下线，触发新一轮的rebalance操作
3. 如果HeadbeatResponse中带有IllegalGeneration异常，说明GroupCoordinator发起了rebalance操作，此时消费者发送JoinGroupRequest给GroupCoordinator.JoinGroupRequest的主要目的是为了统治GroupCoordinator，当前消费者要加入指定的consumer group。
    之后，GroupCoordinator会根据收到的JoinGroupRequest和zookeeper中的元数据完成对此consumer group的分区分配
4. GroupCoordinator会在分配完成后，将分配结果写入zookeeper保存，并通过JoinGroupResponse返回给消费者。消费者就可以根据JoinGroupResponse中分配的分区开始消费数据
5. 消费者成功称为consumer group的成员后，会周期性的发送HeadbeatReqeust。如果HeadbeatResponse包含IllegalGeneration异常，则执行步骤3。
    如果找不到对应的GroupCoordinator，则周期性的执行步骤1，直至成功。
    
缺点：
- 分区分配的操作是在服务端的GroupCoordinator中完成的，这就要求服务端实现partition的分配策略。当使用新的partition分配策略时，就必须修改服务端的代码或配置，之后重启服务，比较麻烦
- 不同的rebalance策略有不同的验证需求，当需要自定义分区分配策略和验证需求时，就会很麻烦

####方案三####
kafka0.9对上述方案进行了重新设计，将分区分配的工作放到了消费者这一段进行处理，而consumer group管理的工作则由GroupCoordinator处理。

对上一方案的JoinGroupRequest的处理过程拆分成了两个阶段，分别是join group阶段和synchronizing group state阶段。

当消费者查找到管理当前consumer group的GroupCoordinator后，就会进入join group阶段，consumer首先向GroupCoordinator发送JoinGroupRequest请求，其中包含消费者的相关信息；
服务端的GroupCoordinator收到JoinGroupRequest后会暂存消息，收集到全部消费者后，根据JoinGroupRequest中的信息来确定consumer group中可用的消费者，从中选取一个消费者成为group leader，
还会选取使用的分区分配策略，最后将这些信息封装成JoinGroupResponse返回给消费者

虽然每个消费者都会受到JoinGroupResponse，但是只有group leader收到的JoinGroupResponse中封装了所有消费者的信息。当消费者确定自己是group leader后，会根据消费者的信息以及选定的分区分配策略进行分区分配。

在synchronizing group state阶段，每个消费者会发送SyncGroupRequest到GroupCoordinator，但是只有group leader的SyncGroupRequest请求包含了分区的分配结果，形成SyncGroupResponse返回给所有consumer。
消费者受到SyncGroupResponse后进行分析，即可获取分配给自身的分区。


####KafkaConsumer####
部分字段：
````
    /** clientId的生成器，如果没有明确指定client的id，则使用字段生成一个id */
    private static final AtomicInteger CONSUMER_CLIENT_ID_SEQUENCE = new AtomicInteger(1);
    /** consumer的唯一标识 */
    private final String clientId;
    /** 控制consumer与服务器GroupCoordinator之间的通信逻辑 */
    private final ConsumerCoordinator coordinator;
    /** key反序列化器 */
    private final Deserializer<K> keyDeserializer;
    /** value反序列化器 */
    private final Deserializer<V> valueDeserializer;
    /** 负责从服务端获取消息 */
    private final Fetcher<K, V> fetcher;
    /** ConsumerInterceptor集合 */
    private final ConsumerInterceptors<K, V> interceptors;

    /** 负责消费者与kafka服务端的网络通信 */
    private final ConsumerNetworkClient client;
    /** 维护消费者的消费状态 */
    private final SubscriptionState subscriptions;
    /** 记录整个儿kafka集群的元信息 */
    private final Metadata metadata;
    /** 记录当前使用KafkaConsumer的线程的id */
    private final AtomicLong currentThread = new AtomicLong(NO_CURRENT_THREAD);
    /** 记录当前使用KafkaConsumer的线程的重入次数 */
    private final AtomicInteger refcount = new AtomicInteger(0);
````

####ConsumerNetworkClient####
在NetworkClient之上进行封装，提供更高级的功能和api

部分字段：
````
    /** NetworkClient对象 */
    private final KafkaClient client;
    /** 由调用KafkaConsumer对象的消费者线程之外的其他线程设置，表示要中断KafkaConsumer线程 */
    private final AtomicBoolean wakeup = new AtomicBoolean(false);
    /** 定时任务队列（此任务是心跳任务） */
    private final DelayedTaskQueue delayedTasks = new DelayedTaskQueue();
    /** 缓冲队列 
    private final Map<Node, List<ClientRequest>> unsent = new HashMap<>();
    /** 用于管理kafka集群元数据 */
    private final Metadata metadata;
    /** ClientRequest在unsent中缓存的超时时长 */
    private final long unsentExpiryMs;
    /** KafkaConsumer是否正在执行不可中断的方法。每进入一个不可中断的方法时，则加1，退出不可中断方法时，则减少1。wakeupDisabledCount只会被KafkaConsumer线程修改，其他线程不能修改 */
    private int wakeupDisabledCount = 0;
````

####SubscriptionState####

KafkaConsumer从kafka拉取消息时发送到请求是FetchRequest,在其中需要制定消费者希望拉取的起始消息的offset。
为了快速或者这个值，KafkaConsumer使用SubscriptionState来跟踪TopicPartition与offset对应关系
部分字段

````
    /** SubscriptionType枚举类型，表示订阅的模式 */
    private SubscriptionType subscriptionType;
    /** 使用AUTO_PATTERN模式时，是按照此字段记录的正则表达式对所有topic进行匹配，对匹配符合的topic进行订阅 */
    private Pattern subscribedPattern;
    /** 如果使用AUTO_TOPICS或AUTO_PATTERN模式，则使用此集合记录所有订阅的topic */
    private final Set<String> subscription;
    /** leader记录Consumer Group中所有消费者订阅的topic，其他的follower保存自身订阅的topic */
    private final Set<String> groupSubscription;
    /** 使用USER_ASSIGNED模式，此集合记录了分配给当前消费者的TopicPartition集合 */
    private final Set<TopicPartition> userAssignment;
    /** 无论使用什么订阅模式，都使用此集合记录每个TopicPartition的消费状态 */
    private final Map<TopicPartition, TopicPartitionState> assignment;
    /** 标记是否需要进行一次分区分配 */
    private boolean needsPartitionAssignment;
    /** 标记是否需要从GroupCoordinator获取最近提交的offset。当出现异步提交offset操作或是rebalance操作刚完成时，会将其置为true，成功获取最近提交offset后会设置为false */
    private boolean needsFetchCommittedOffsets;
    /** 默认OffsetResetStrategy策略 */
    private final OffsetResetStrategy defaultResetStrategy;
    /** ConsumerRebalanceListener类型，用于监听分区分配操作。 */
    private ConsumerRebalanceListener listener;
````



####ConsumerCoordinator####
在KafkaConsumer中通过ConsumerCoordinator组件实现与服务端的GroupCoordinator交互，
ConsumerCoordinator继承自AbstractCoordinator

- AbstractCoordinator

部分字段：
````
    /** 心跳任务的辅助类 */
    private final Heartbeat heartbeat;
    /** 心跳定时任务，负责定时发送心跳请求和心跳响应的处理，会被添加到ConsumerNetworkClient.delayedTasks定时任务队列中 */
    private final HeartbeatTask heartbeatTask;
    /** 当前消费者所属的consumer group的id */
    protected final String groupId;
    /** ConsumerNetworkClient对象，负责网络通信和执行定时任务 */
    protected final ConsumerNetworkClient client;
    /** 标记是否需要执行发送JoinGroupRequest */
    private boolean needsJoinPrepare = true;
    /** 是否重新发送JoinGroupRequest请求的条件之一 */
    private boolean rejoinNeeded = true;
    /** 记录服务端GroupCoordinator所在的node节点 */
    protected Node coordinator;
    /** 服务端GroupCoordinator返回的分配给消费者的唯一id */
    protected String memberId;
    /** 服务端GroupCoordinator返回的年代信息，用来区分两次rebalance操作。由于网络延迟问题，在执行rebalance操作时可能受到上次rebalance过程的请求，为了避免这种干扰，每次rebalance操作都会递增 */
    protected int generation;
````

ConsumerCoordinator部分字段：
````
    /**
     * PartitionAssignor列表。在消费者发送的JoinGroupRequest请求中包含了消费者自身支持的PartitionAssignor信息
     * GroupCoordinator从所有消费者都支持的分配策略中选择一个，通知leader使用此分配策略进行分区分配。
     * 此字段的值通过partition.assignment.strategy参数配置，可以配置多个
     */
    private final List<PartitionAssignor> assignors;
    /** 记录kafka集群的元数据 */
    private final Metadata metadata;
    /** SubscriptionState对象 */
    private final SubscriptionState subscriptions;
    /** 是否开启了自动提交offset */
    private final boolean autoCommitEnabled;
    /** 自动提交offset的定时任务 */
    private final AutoCommitTask autoCommitTask;
    /** ConsumerInterceptor集合 */
    private final ConsumerInterceptors<?, ?> interceptors;
    /** 标识是否排除内部的topic */
    private final boolean excludeInternalTopics;
    /**
     * 用来存储Metadata的快照信息，主要用来检测topic是否发送了分区数量的变化。
     * 在ConsumerCoordinator的构造方法中，会为Metadata添加一个监听器，当Metadata更新时会做下面几件事
     * 1. 如果是AUTO_PATTERN模式，则使用用户自定义的正则表达式过滤topic，得到需要订阅的topic集合后，设置到SubscriptionState的subscription集合和groupSubscription集合中
     * 2. 如果是AUTO_PATTERN或AUTO_TOPICS模式，为当前Metadata做一个快照，这个快照底层使用HashMap记录每个topic中Partition的个数。
     *  将新旧快照进行比较，发生变化的话，则表示消费者订阅的topic发生分区数量变化，则将SubscriptionState的needsPartitionAssignment字段置为true，需要重新进行分区分配
     * 3. 使用metadataSnapshot字段记录变化后的新快照
     */
    private MetadataSnapshot metadataSnapshot;
    /**
     * 用来存储Metadata的快照信息，不过是用来检测Partition分配的过程中没有发生分区数量变化。
     * 具体是在leader消费者开始分区分配操作前，使用此字段记录Metadata快照；
     * 收到SyncGroupResponse后，会比较此字段记录的快照与当前Metadata是否发生变化。如果发生变化，则要重新继续分区分配
     */
    private MetadataSnapshot assignmentSnapshot;
````

####PartitionAssignor####
leader消费者在收到JoinGroupResponse后，会按照其中指定的分区分配策略进行分区分配，每个分区分配策略就是一个PartitionAssignor接口的实现

PartitionAssignor接口中自定义了Subscription和Assignment两个内部类。进行分区分配需要的两方面的数据：Metadata中记录的集群元数据和每个Member的订阅信息。
为了用户增强分配结果的控制，就将用户订阅信息和一些影响分配的用户自定义信息封装成Subscription，例如，“用户自定义数据”可以是每个消费者的权重

Subscription字段
````
    /** 表示某个Member订阅的topic集合*/
    private final List<String> topics;
    /** 表示用户自定义的数据*/
    private final ByteBuffer userData;
````

Assignment字段:
````
    /** 分配给消费者的TopicPartition集合 */
    private final List<TopicPartition> partitions;
    /** 用户自定一点数据 */
    private final ByteBuffer userData;
````

**分区分配策略**
1. RangeAssignor：分区分配策略：针对每个topic，n=分区/消费者数量，m=分区数%消费者数量，前m个消费者分配n+1个分区，后面的(消费者数量-m)个消费者每个分配n个partition
2. RoundRobinAssignor：分区分配策略：将所有topic的partition按照字典序排序，然后对每个consumer进行轮询分配。

例如，有C0.C1两个消费者和t0.t1两个topic，每个topic有三个分区编号都是0~2
使用RangeAssignor的分配结果是:C0:[t0p0,t0p1,t1p0,t1p1],C1:[t0p2,t1p2]
使用RoundRobinAssignor策略是：C0:[t0p0,t0p2,t1p1],C1:[t0p1,t1p0,t1p2]

####Heartbeat####
消费者定期向服务端的GroupCoordinator发送HeartbeatRequest来确定彼此在线

task.run流程图：
![](/picture/HeartbeatTask-run-flow.png)

1. 首先检查是否需要发送HeartbeatRequest，条件有多个。如果不符合条件，则不再执行HeartbeatTask，等待后续调用reset()方法重启HeartbeatTask任务
     a. GroupCoordinator已确定且已连接
     b. 不处于正在等待Partition分配结果的状态
     c. 之前HeartbeatRequest请求正常收到响应且没有过期
2. 调用Heartbeat.sessionTimeoutExpired()函数，检测HeartbeatResponse是否超时。
 若超时，则认为GroupCoordinator宕机，调用coordinatorDead()函数清空其unsent集合中对应的请求队列，
 并将这些请求标记为异常后结束，将coordinator字段设置为null,表示将重新选择GroupCoordinator。
 同时还会停止HeartbeatTask的执行。
3. 检测HeartbeatTask是否到期，如果不到期则更新其到期时间，将HeartbeatTask对象重新添加到DelayedTaskQueue中，等待其到期执行；
 如果已到期则继续后面的步骤，发送HeartbeatRequest请求
4. 更新最近一次发送HeartbeatRequest请求的时间，将requestInFlight设置为true,表示有未响应的HeartbeatRequest请求，防止重复发送
5. 创建HeartbeatRequest请求，并调用ConsumerNetworkClient.send()函数，将请求放入unsent集合中缓存，并返回RequestFuture<Void>
 在后面的ConsumerNetworkClient.poll()操作中会将其发送个GroupCoordinator。
6. 在RequestFuture<Void>对象上添加RequestFutureListener.

####rebalance实现####
以下几种情况会触发Rebalance操作：
1. 有新的消费者加入Consumer Group
2. 有消费者宕机下线。消费者并不一定需要真正下线，例如遇到长时间的GC、网络延迟导致消费者长时间未向GroupCoordinator发送HeartbeatRequest时，GroupCoordinator会认为消费者下线
3. 有消费者主动退出Consumer Group
4. Consumer Group 订阅的任一Topic出现分区数量的变化
5. 消费者调用unsubscrible()取消对某topic的订阅

- 第一阶段
rebalance操作的第一步就是查找GroupCoordinator，这个阶段消费者会向kafka集群中的任意一个borker发送GroupCoordinatorRequest请求，并处理返回的GroupCoordinatorResponse响应

发送GroupCoordinatorRequest的入口是ConsumerCoordinator.ensureCoordinatorReady()
流程图如下：

![](/picture/ensureCoordinatorReady-flow.png)

1. 首先检测是否需要重新查找GroupCoordinator，主要是检查coordinator字段是否为空，以及与GroupCoordinator之间的连接是否正常
2. 查找集群负载最低的Node节点，并创建GroupCoordinatorRequest请求。调用client.send()方法将请求放入unsent队列中等待发送，并返回RequestFuture<Void>对象，返回的RequestFuture<Void>对象经过了compose()方法适配
3. 调用ConsumerNetworkClient.poll(future)方法，将GroupCoordinatorRequest请求发送出去。次数使用阻塞的方式发送，直到收到GroupCoordinatorResponse响应或异常完成，才从此方法返回
4. 检查RequestFuture<Void>对象的状态。如果按RetriableException异常，则调用ConsumerNetworkClient.awaitMetadataUpdate()方法则色更新Metadata中记录的集群元数据后跳转到步骤1继续执行。如果不是，RetriableException异常则直接报错
5. 如果成功找到GroupCoordinator节点，但是网络连接失败，则将其unsent中对应的请求清空，并将coordinator字段置为null，准备重新查找GroupCoordinator，退避一段时间后跳转到步骤1继续执行

- 第二阶段
在成功查找到对应的GroupCoordinator之后进入Join Group阶段。在此阶段，消费者会向GroupCoordinator发送JoinGroupRequest请求，并处理响应

发送流程是ConsumerCoordinator.ensurePartitionAssignment()函数
流程图如下：

![](/picture/ensurePartitionAssignment-flow.png)

1. 调用SubscriptionState.partitionsAutoAssigned()方法，检测Consumer的订阅是否是AUTO_TOPIC或AUTO_PATTERN。因为USER_ASSIGNED不需要进行rebalance操作，而是由用户手动指定分区
2. 如果订阅模式是AUTO_PATTERN，则检查Metadata是否需要更新。在ConsumerCoordinator的构造函数中为Metadata添加了监听器，当Metadata更新时就会使用SubscriptionState中的正则表达式过滤Topic，并更改SubscriptionState中的订阅信息。
	同时也会使用metadataSnapshot字段记录当前的Metadata的快照。这里要更新Metadata的原因是，为了防止因使用过期的Metadata进行Rebalance操作而导致多次连续的Rebalance操作
3. 调用ConsumerCoordinator.needRejoin()方法判断是要发送JoinGroupRequest加入ConsumerGroup,其实现是检测是否使用了AUTO_TOPICS或AUTO_PATTERN模式，检测rejoinNeeded和needsPartitionAssignment两个字段的值。
4. 调用onJoinPrepare()方法进行发送JoinGroupRequest请求之前的准备，做了三件事
	1. 如果开启了自动提交offset则进行同步提交offset，此步骤可能会阻塞线程
	2. 调用注册SubscriptionState中的ConsumerRebalanceListener上的回调方法
	3. 将SubscriptionState的needsPartitionAssignment字段设置为true并收缩groupSubscription集合
5. 再次调用needRejoin()方法检测，之后调用ensureCoordinatorReady()方法检测已找到GroupCoordinator且与之建立了连接
6. 如果还有发往GroupCoordinator所在Node的请求，则阻塞等待这些请求全部发送完成并收到响应(即等待unsent及InFlightRequests的对应队列为空)，然后返回步骤5继续执行，主要是为了避免重复发送JoinGroupRequest请求
7. 调用sendJoinGroupRequest()方法创建JoinGroupRequest请求，并调用ConsumerNetworkClient.send()方法将请求放入unsent中缓存，等待发送
8. 在步骤7返回的RequestFuture<ByteBuffer>对象上添加RequestFutureListener
9. 调用ConsumerNetworkClient.poll()函数发送JoinGroupRequest,这里会阻塞等待，直到收到JoinGroupResponse或出现异常
10. 检测RequestFuture.fail()。如果出现RetriableException异常则进行重试，其他异常则报错，如果污异常，则整个第二阶段操作完成

JoinGroupResponse的处理流程是JoinGroupResponseHandler.handle()函数
流程图如下：

![](/picture/join-group-response-handle.png)

1. 解析JoinGroupResponse，获取GroupCoordinator分配的memberId,generation等信息，更新到本地
2. 消费者根据leaderId检查自己是不是leader,如果是leader则进入onJoinLeader()函数，如果不是leader则进入onJoinFollower()函数。下面主要将onJoinLeader()函数
3. leader根据JoinGroupResponse的group_protocol字段指定的Partition分配策略，查找响应的PartitionAssignor对象
4. leader将JoinGroupResponse的member字段进行反序列化，得到consumer group中全部消费者订阅的topic，leader会将这些topic信息添加到SubscriptionState.groupSubscription集合中。而follower则只关心自己订阅的topic信息
5. 第4步可能有新的topic添加进来，所以更新Metadata信息
6. 等待Metadata更新完成后，会在assignmentSnapshot字段中存储一个Metadata快照（即通过Metadata的Listener创建的快照）
7. 调用PartitionAssignor.assign()函数进行分区分配
8. 将分配结果序列化，保存到Map中返回，其中key是消费者的memberId,value是分配结果序列化后的ByteBuffer

- 第三阶段
完成分区分配之后就进入Synchronizing Group State阶段，主要逻辑是向GroupCoordinator发送SyncGroupRequest请求并处理SyncGroupResponse响应。
发送SyncGroupRequest请求的逻辑是在分区分配操作完成后,在onJoinLeader()方法中完成的
1. 得到序列化后的分区分配结果后，leader将其封装成SyncGroupRequest，而Follower形成的SyncGroupRequest中这部分为空集合
2. 调用ConsumerNetworkClient.send()方法将请求放入unsent集合中等待发送

从SyncGroupResponse中得到的分区分配结果最终由ConsumerCoordinator.onJoinComplete()函数处理，调用此方法是在第二阶段ensureActiveGroup()函数中添加的RequestFutreListener中调用

onJoinComplete()流程图如下：
![](/picture/onJoinComplete-flow.png)

1. 在第二阶段leader开始分配分区之前，leader使用assignmentSnapshot字段记录了Metadata快照。此时在leader中，将此快照与最新的Metadata快照进行对比。如果快照不一致则表示分区分配过程中出现了topic增删或分区数量的变化，则将needsPartitionAssignment置true，需重新进行分区分配
2. 反序列化拿到分配给当前消费者的分区，并添加到SubscriptionState.assignment集合中，之后消费者会按照此集合指定的分区进行消费，将needsPartitionAssignment置为false
3. 调用PartitionAssignor的onAssignment()回调函数，默认是空实现。
4. 如果开启了自动提交offset的功能，则重新启动AutoCommitTask定时任务
5. 调用SubscriptionState中注册的ConsumerRebalanceListener
6. 将needsJoinPrepare重置为true,为下次rebalance操作做准备
7. 重启HeartbeatTask定时任务，定时发送心跳

####offset操作####






