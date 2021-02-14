package top.itning.generic.service.common.model;

import lombok.Data;

/**
 * @author itning
 * @since 2021/1/28 11:09
 */
@Data
public class RestModel<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> RestModel<T> failed(String message) {
        RestModel<T> restModel = new RestModel<>();
        restModel.setSuccess(false);
        restModel.setMessage(message);
        return restModel;
    }

    public static RestModel<?> success() {
        return success("success", null);
    }

    public static <T> RestModel<T> success(T data) {
        return success("success", data);
    }

    public static <T> RestModel<T> success(String message, T data) {
        RestModel<T> restModel = new RestModel<>();
        restModel.setSuccess(true);
        restModel.setMessage(message);
        restModel.setData(data);
        return restModel;
    }
}
