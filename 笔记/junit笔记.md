# junit断言调试函数 #

#### assertEquals断言 ####

它的作用是比较实际的值和用户预期的值是否一样

#### assertTrue与assertFalse断言 ####

assertTrue与assertFalse可以判断某个条件是真还是假，如果和预期的值相同则测试成功，否则将失败

#### assertNull与assertNotNull断言 ####

assertNull与assertNotNull可以验证所测试的对象是否为空或不为空，如果和预期的相同则测试成功，否则测试失败

#### assertSame与assertNotSame断言 ####

assertSame和assertEquals不同，assertSame测试预期的值和实际的值是否为同一个参数(即判断是否为相同的引用)。assertNotSame则测试预期的值和实际的值是不为同一个参数。而assertEquals则判断两个值是否相等，通过对象的equals方法比较，可以相同引用的对象，也可以不同。

#### fail断言 ####

“fail”断言能使测试立即失败，这种断言通常用于标记某个不应该被到达的分支。例如assertTrue断言中，condition为false时就是正常情况下不应该出现的，所以测试将立即失败


123