package sesac.intruders.piggybank.global.error.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import sesac.intruders.piggybank.global.error.ErrorCode;

@Getter
@AllArgsConstructor
public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;
}
