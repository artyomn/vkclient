package vkclient.vkclient.data.entity;

public class VkSavedFile extends BaseVkResponse{
    public static class VkResponse{
        public int id;
        public int owner_id;
    }
    public VkResponse[] response;
}
