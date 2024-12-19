package kr.inuappcenterportal.inuportal.global.exception.ex;

import lombok.Getter;

@Getter
public class MyException extends RuntimeException{
    private MyErrorCode errorCode;

    public MyException(MyErrorCode errorCode){
        super(errorCode.getMessage()); // 부모 RuntimeException에 메시지 전달
        this.errorCode = errorCode;
    }
}
