package top.itning.generic.service.core.resolver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Optional;

/**
 * 控制层适配器：主要做错误处理
 *
 * @author itning
 * @since 2020/10/22 13:46
 */
@Slf4j
@ControllerAdvice
public class ExceptionResolver {

    /**
     * 验证框架验证失败时返回给前端处理
     *
     * @param response {@link HttpServletResponse}
     * @param e        {@link MethodArgumentNotValidException}
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseBody
    public void methodArgumentNotValidExceptionHandler(HttpServletResponse response, MethodArgumentNotValidException e) {

        List<ObjectError> allErrors = e.getBindingResult().getAllErrors();

        String message = Optional.of(allErrors)
                .flatMap(errorItem -> errorItem.stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .reduce((a, b) -> a + " " + b))
                .orElse("");

        log.warn(message);

        response.setStatus(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * 验证框架验证失败时返回给前端处理
     *
     * @param response {@link HttpServletResponse}
     * @param e        {@link MethodArgumentNotValidException}
     */
    @ExceptionHandler(value = ConstraintViolationException.class)
    @ResponseBody
    public void constraintViolationExceptionHandler(HttpServletResponse response, ConstraintViolationException e) {

        log.warn(e.getMessage());

        response.setStatus(HttpStatus.BAD_REQUEST.value());
    }

    @ExceptionHandler(value = NoHandlerFoundException.class)
    public void noHandlerFoundExceptionHandler(HttpServletResponse response) {
        response.setStatus(HttpStatus.NOT_FOUND.value());
    }
}
