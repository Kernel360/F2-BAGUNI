package baguni.common.util;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class MeasureTimeAspect {

	@Pointcut("@annotation(baguni.common.annotation.MeasureTime)")
	public void pointcut() {
	}

	@Around("pointcut()")
	public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
		var methodName = joinPoint.getSignature().getName();
		long startTime = System.currentTimeMillis();
		var result = joinPoint.proceed();
		long endTime = System.currentTimeMillis();
		log.info("{} 실행 시간 : {} ms", methodName, endTime - startTime);
		return result;
	}
}
