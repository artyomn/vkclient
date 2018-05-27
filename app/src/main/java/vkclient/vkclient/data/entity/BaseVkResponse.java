package vkclient.vkclient.data.entity;

public abstract class BaseVkResponse {
    public class VkError{
        public int error_code;
        public String error_msg;
    }
    VkError error;
}
