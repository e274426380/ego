package com.ego;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Map<String,Object>> exceptionHandler(Exception e, HttpServletRequest request){
        log.error("Global Exception Occured => url : {}", request.getRequestURL());
        //检测是否是自定义异常，如果是响应详细信息
        if(e instanceof EgoException){
            EgoException exception=(EgoException) e;
            //利用Map实现VO(也可以自定义VO)
            Map<String, Object> resultVO = new HashMap<>(2);
            resultVO.put("errorCode", exception.getErrorCode());
            resultVO.put("errorMessage", exception.getErrorMessage());
            //将具体错误信息响应给客户端
            return ResponseEntity.status(exception.getErrorCode()).body(resultVO);
        }

        // 如果不是，直接返回500
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

}
