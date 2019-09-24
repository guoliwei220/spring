/*
package com.chngc.collect.autoconfigure;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

*/
/**
 * Created by sunrm on 2017/11/9.
 *//*

@Configuration
@EnableConfigurationProperties({RedisProperties.class})
public class RedisConfiguration {
    private final RedisProperties properties;

    public RedisConfiguration(RedisProperties properties) {
        this.properties = properties;
    }

    */
/**
     * 老redis
     * @return
     *//*

    @Bean
    public JedisPool jedisPool(JedisPoolConfig jedisPoolConfig) {
        JedisPool jedisPool = new JedisPool(jedisPoolConfig, this.properties.getHost(), this.properties.getPort(), (int) this.properties.getTimeout().toMillis());
        return jedisPool;
    }

    */
/**
     * redis连接池配置
     * @return
     *//*

    @Bean
    public JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig config = new JedisPoolConfig();
        RedisProperties.Pool props = this.properties.getLettuce().getPool();
        config.setMaxTotal(props.getMaxActive());
        config.setMaxIdle(props.getMaxIdle());
        config.setMinIdle(props.getMinIdle());
        config.setMaxWaitMillis(props.getMaxWait().toMillis());
        return config;
    }
}
*/
