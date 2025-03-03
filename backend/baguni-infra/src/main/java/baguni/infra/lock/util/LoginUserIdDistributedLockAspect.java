package baguni.infra.lock.util;

import java.lang.reflect.Field;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import baguni.infra.annotation.LoginUserIdDistributedLock;
import baguni.infra.lock.LockProvider;

@Order(1)
@Aspect
@Component
@RequiredArgsConstructor
public class LoginUserIdDistributedLockAspect {

	private final LockProvider lockProvider;

	@Around("@annotation(loginUserIdDistributedLock)")
	public Object handleDistributedLock(
		ProceedingJoinPoint joinPoint,
		LoginUserIdDistributedLock loginUserIdDistributedLock
	) throws Throwable {
		String key = getMethodName(joinPoint);
		long timeout = loginUserIdDistributedLock.timeout();
		Long userId = getUserIdFromArgs(joinPoint);

		boolean lockCheck = lockProvider.acquireLock(key, timeout, userId);
		if (!lockCheck) {
			throw new LockException("락 설정 실패 : " + userId);
		}

		try {
			return joinPoint.proceed();
		} finally {
			lockProvider.releaseLock(key, userId);
		}
	}

	/**
	 * @author sangwon
	 * 리플렉션을 통해 메서드 이름을 가져오는 메서드
	 */
	private String getMethodName(JoinPoint joinPoint) {
		MethodSignature signature = (MethodSignature)joinPoint.getSignature();
		return signature.getMethod().getName();
	}

	/**
	 * @author sangwon
	 * 리플렉션을 통해 메서드 파라미터에 있는 userId를 가져온다.
	 */
	private Long getUserIdFromArgs(JoinPoint joinPoint) {
		Object[] args = joinPoint.getArgs();
		MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
		String[] parameterNames = methodSignature.getParameterNames();

		for (int i = 0; i < parameterNames.length; i++) {
			Object arg = args[i];

			if ("userId".equals(parameterNames[i]) && arg instanceof Long) {
				return (Long)arg;
			}

			if (arg != null) {
				try {
					// Reflection으로 "userId" 필드를 추출
					Field field = arg.getClass().getDeclaredField("userId");
					field.setAccessible(true); // private 필드 접근 허용
					Object userId = field.get(arg);
					if (userId instanceof Long) {
						return (Long)userId;
					}
				} catch (NoSuchFieldException | IllegalAccessException ignored) {
					// 해당 파라미터에 "userId"가 없으면 무시
				}
			}
		}

		throw new IllegalArgumentException("userId 파라미터를 찾을 수 없습니다.");
	}
}
