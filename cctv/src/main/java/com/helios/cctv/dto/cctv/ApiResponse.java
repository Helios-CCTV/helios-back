package com.helios.cctv.dto.cctv;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private int code;
    private String message;
    private T data;

    // 성공 응답용 팩토리 메서드
    public static <T> ApiResponse<T> ok(T data, int code) {
        return new ApiResponse<>(true, code, "요청에 성공했습니다.", data);
    }

    // 실패 응답용 팩토리 메서드
    public static <T> ApiResponse<T> fail(String message, int code) {
        return new ApiResponse<>(false, code, message, null);
    }

    //이걸로 반환하는거 먼저 해보고 고치기
}
