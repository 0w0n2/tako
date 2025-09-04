package com.bukadong.tcg;


import com.bukadong.tcg.common.base.BaseResponse;
import com.bukadong.tcg.common.base.BaseResponseStatus;
import com.bukadong.tcg.common.exception.BaseException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/health")
public class TestController {

	@Operation(summary = "헬스 체크", description = "서버 상태 확인용 API (200 OK)")
	@Parameters({
			@Parameter(name = "name", description = "사용자 이름", required = true),
			@Parameter(name = "value", description = "전달할 값", required = true)
	})
	@GetMapping
	public BaseResponse<Void> healthCheck() {
		return BaseResponse.ok(); // ok 메서드는 200 OK 상태 코드와 함께 빈 응답 반환
	}

	@GetMapping("/error")
	@Operation(summary = "헬스 체크 에러", description = "서버 상태 확인용 API (에러 발생)")
	public BaseResponse<Void> healthCheckError(@RequestParam String name) {
		// 강제로 예외 발생. 상태값들은 알아서 생성하도록
		throw new BaseException(BaseResponseStatus.DISALLOWED_ACTION); // 실제로는 서비스 로직에서 발생되어야 함
	}

	/**
	 * 	주석 풀면 에러남.  BaseResponse<> 안에 다른형의 데이터 값 넣으면 컴파일 에러 난다는걸 말해주는거.
 	 */
	/*@GetMapping("/not-mapped")
	public BaseResponse<String> notMapped(@RequestParam String name) {
		return BaseResponse.of(123);
	}*/
	
}
