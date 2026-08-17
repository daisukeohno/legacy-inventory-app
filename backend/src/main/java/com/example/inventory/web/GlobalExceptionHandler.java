package com.example.inventory.web;

import com.example.inventory.service.EmptyOrderException;
import com.example.inventory.service.InsufficientStockException;
import com.example.inventory.service.ProductNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 旧実装の System.out.println による握りつぶしを置き換えるグローバル例外ハンドラ。
 * Spring MVC 標準例外（型不一致・未定義パス・非対応メソッド等）は
 * {@link ResponseEntityExceptionHandler} により本来のステータスを維持したまま ApiError 形式で返す。
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
                                                                 HttpHeaders headers,
                                                                 HttpStatusCode status,
                                                                 WebRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        log.info("Validation failed: {}", fieldErrors);
        ApiError body = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "入力内容に誤りがあります。",
                fieldErrors,
                List.copyOf(fieldErrors.values()));
        return handleExceptionInternal(e, body, headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException e,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        log.info("Malformed request body: {}", e.getMessage());
        ApiError body = ApiError.of(HttpStatus.BAD_REQUEST.value(), "リクエストの形式が正しくありません。");
        return handleExceptionInternal(e, body, headers, HttpStatus.BAD_REQUEST, request);
    }

    /** 標準例外のステータスは維持しつつ、レスポンス本体を ApiError に揃える。 */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception e,
                                                            Object body,
                                                            HttpHeaders headers,
                                                            HttpStatusCode statusCode,
                                                            WebRequest request) {
        Object resolvedBody = (body instanceof ApiError)
                ? body
                : ApiError.of(statusCode.value(), messageFor(statusCode));
        if (!(body instanceof ApiError)) {
            log.info("Request rejected with status {}: {}", statusCode.value(), e.getMessage());
        }
        return super.handleExceptionInternal(e, resolvedBody, headers, statusCode, request);
    }

    private String messageFor(HttpStatusCode statusCode) {
        return switch (statusCode.value()) {
            case 400 -> "リクエストの形式が正しくありません。";
            case 404 -> "リソースが見つかりません。";
            case 405 -> "許可されていないHTTPメソッドです。";
            case 415 -> "サポートされていないメディアタイプです。";
            default -> "リクエストを処理できませんでした。";
        };
    }

    @ExceptionHandler(EmptyOrderException.class)
    public ResponseEntity<ApiError> handleEmptyOrder(EmptyOrderException e) {
        log.info("Rejected empty order: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiError.of(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiError> handleInsufficientStock(InsufficientStockException e) {
        log.info("Insufficient stock for productId={} requested={} available={}",
                e.getProductId(), e.getRequestedQuantity(), e.getAvailableQuantity());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of(HttpStatus.CONFLICT.value(), e.getMessage()));
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ProductNotFoundException e) {
        log.info("Product not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of(HttpStatus.NOT_FOUND.value(), e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception e) {
        log.error("Unexpected error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), "サーバ内部エラーが発生しました。"));
    }
}
