package com.kh.spring.common.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class AfterThrowingTest {

	private Logger logger = LoggerFactory.getLogger(AfterThrowingTest.class);
	
	// @AfterThrowing : 메소드실행 이후에 발생하는 예외를 얻어오는 어노테이션
	// 		 Throwing : 반환할 예외값을 지정한 매개변수명 지정하는 속성
	@AfterThrowing(pointcut="CommonPointcut.implPointcut()", throwing="exceptionObj")
	public void serviceReturnValue(JoinPoint jp, Exception exceptionObj) {
		StringBuilder sb = new StringBuilder("Exception : " + exceptionObj.getStackTrace()[0]);
		sb.append("\n에러 메세지 : " + exceptionObj.getMessage());
		logger.error(sb.toString());
	}
	
}
