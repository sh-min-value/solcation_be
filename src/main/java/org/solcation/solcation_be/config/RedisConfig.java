package org.solcation.solcation_be.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.solcation.solcation_be.domain.notification.dto.PublishDTO;
import org.solcation.solcation_be.domain.notification.dto.PushNotificationDTO;
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
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
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

    /* 공용 ObjectMapper 등록 */
    @Bean
    public ObjectMapper redisObjectMapper() {
        return new ObjectMapper()
                .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /* 공용 JSON serializer 등록 - (key,val) 저장용 */
    @Bean
    public Jackson2JsonRedisSerializer<PushNotificationDTO> redisJsonSerializer() {
        return new Jackson2JsonRedisSerializer<>(redisObjectMapper(), PushNotificationDTO.class);
    }


    /* RedisTemplate 빈 생성 - (key,val) 저장용 */
    @Bean
    public RedisTemplate<String, PushNotificationDTO> redisTemplate() {
        var t = new RedisTemplate<String, PushNotificationDTO>();

        t.setConnectionFactory(redisConnectionFactory());
        t.setKeySerializer(new StringRedisSerializer());
        t.setValueSerializer(redisJsonSerializer());

        return t;
    }

    /* 공용 JSON serializer 등록 - pub/sub용 */
    @Bean
    public Jackson2JsonRedisSerializer<PublishDTO> pubsubSerializer() {
        return new Jackson2JsonRedisSerializer<>(redisObjectMapper(), PublishDTO.class);
    }

    /* Redis pub/sub 메시지 리스너 컨테이너 */
    @Bean
    public RedisTemplate<String, PublishDTO> pubSubTemplate() {
        var t = new RedisTemplate<String, PublishDTO>();
        t.setConnectionFactory(redisConnectionFactory());
        t.setKeySerializer(new StringRedisSerializer());
        t.setValueSerializer(pubsubSerializer());
        t.afterPropertiesSet();
        return t;
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisSubscriber redisSubscriber) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory());
        var ser = pubsubSerializer();
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
