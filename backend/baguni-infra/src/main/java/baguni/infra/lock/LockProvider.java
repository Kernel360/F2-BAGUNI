package baguni.infra.lock;

/**
 * @author sangwon
 * MySQL, Redis를 이용하여 분산 락을 구현하기 위한 인터페이스
 * MySQL, Redis 구현체를 쉽게 갈아끼우기 위해 인터페이스 선언
 */
public interface LockProvider {

	boolean acquireLock(String key, long timeout, Long userId);

	void releaseLock(String key, Long userId);
}
