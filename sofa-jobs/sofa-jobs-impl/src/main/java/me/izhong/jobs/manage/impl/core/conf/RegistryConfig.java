package me.izhong.jobs.manage.impl.core.conf;

public class RegistryConfig {
    public static final int BEAT_TIMEOUT = 30;
    public static final int DEAD_TIMEOUT = BEAT_TIMEOUT * 3;

    public enum RegistType{ EXECUTOR, ADMIN }
}
