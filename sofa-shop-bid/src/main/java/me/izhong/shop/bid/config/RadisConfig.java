package me.izhong.shop.bid.config;

import me.izhong.shop.bid.rat.RateLimitClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class RadisConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    @Value("${spring.redis.password}")
    private String password;

    @Value("${spring.redis.timeout}")
    private int timeout;

    @Bean
    public JedisPool jedisPool() {
        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(),
                host, port,timeout,password);
        return jedisPool;
    }


    @Bean
    public StringRedisTemplate StringRedisTemplate() {
        RedisStandaloneConfiguration redisStandaloneConfiguration =
                new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(host);
        redisStandaloneConfiguration.setDatabase(0);
        redisStandaloneConfiguration.setPassword(RedisPassword.of(password));
        redisStandaloneConfiguration.setPort(port);

        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        //最大连接数
        jedisPoolConfig.setMaxTotal(10);
        //最小空闲连接数
        jedisPoolConfig.setMinIdle(2);
        //当池内没有可用的连接时，最大等待时间
        jedisPoolConfig.setMaxWaitMillis(1000);
        JedisClientConfiguration.JedisPoolingClientConfigurationBuilder jpcb =
                (JedisClientConfiguration.JedisPoolingClientConfigurationBuilder) JedisClientConfiguration.builder();
        jpcb.poolConfig(jedisPoolConfig);
        JedisClientConfiguration jedisClientConfiguration = jpcb.build();


        JedisConnectionFactory connectionFactory = new JedisConnectionFactory(redisStandaloneConfiguration, jedisClientConfiguration);

        StringRedisTemplate redis = new StringRedisTemplate(connectionFactory);
        redis.afterPropertiesSet();
        return redis;
    }

    @Bean
    public RateLimitClient rateLimitClient(StringRedisTemplate redisTemplate) {
        DefaultRedisScript<Long> rateLimitLua = new DefaultRedisScript<>();
        rateLimitLua.setLocation(new ClassPathResource("rate_limit2.lua"));
        rateLimitLua.setResultType(Long.class);
        RateLimitClient cl = new RateLimitClient(redisTemplate, rateLimitLua);

        return cl;
    }


}
