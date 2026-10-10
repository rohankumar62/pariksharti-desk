package in.pariksharti.desk.exception;

public class ApiException extends RuntimeException {
    private final int status;
    private final String code;

    public ApiException(int status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public int getStatus() { return status; }
    public String getCode() { return code; }

    public static ApiException invalid(String message) {
        return new ApiException(400, "INVALID_INPUT", message);
    }
}