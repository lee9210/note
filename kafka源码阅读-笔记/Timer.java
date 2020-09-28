package a.b.c

public interface Timer{
	void add(TimerTask timerTask);
	Boolean advanceClock(Long timeoutMs);
	Int size();
	void shutdown();
}
class SystemTimer{
	
	public SystemTimer(String executorName){
		
	}
}