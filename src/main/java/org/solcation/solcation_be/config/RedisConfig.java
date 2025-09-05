package org.solcation.solcation_be.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.solcation.solcation_be.domain.notification.dto.PublishDTO;
import org.solcation.solcation_be.util.redis.RedisSubscriber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.notification.channel}")
    private String CHANNEL;

    /* Redis 연결 객체 생성 */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }

    /* RedisTemplate 빈 생성 */
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();

        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }

    /* Redis pub/sub 메시지 리스너 컨테이너 */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisSubscriber redisSubscriber) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory());
        var ser = new GenericJackson2JsonRedisSerializer();
        //구독 설정
        container.addMessageListener((message, pattern) ->{
            String ch = new String(message.getChannel(), java.nio.charset.StandardCharsets.UTF_8);
            Object payload = ser.deserialize(message.getBody());
            redisSubscriber.onMessage(ch, (PublishDTO) payload);
                },
                new PatternTopic(CHANNEL));
        return container;
    }
}
