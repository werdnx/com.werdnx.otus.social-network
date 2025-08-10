package com.werdnx.otus.socialnetwork.service;

import com.werdnx.otus.socialnetwork.amqp.AmqpConfig;
import com.werdnx.otus.socialnetwork.dto.PostCreatedEvent;
import com.werdnx.otus.socialnetwork.model.Post;
import com.werdnx.otus.socialnetwork.repository.FriendRepository;
import com.werdnx.otus.socialnetwork.repository.PostRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class FeedMaterializer {

    private final FriendRepository friends;
    private final PostRepository posts;
    private final RabbitTemplate rabbit;
    private final int chunkSize;

    public FeedMaterializer(FriendRepository friends,
                            PostRepository posts,
                            RabbitTemplate rabbit,
                            @Value("${app.feed.fanout-chunk-size:500}") int chunkSize) {
        this.friends = friends;
        this.posts = posts;
        this.rabbit = rabbit;
        this.chunkSize = chunkSize;
    }

    @RabbitListener(queues = AmqpConfig.QUEUE_MATERIALIZE)
    public void onFanoutTask(@Payload Map<String, Object> task) {
        Long authorId = ((Number) task.get("authorId")).longValue();
        Long postId   = ((Number) task.get("postId")).longValue();

        Post p = posts.findById(postId);
        if (p == null) return;

        var followers = friends.findFollowers(authorId);
        if (followers.isEmpty()) return;

        for (int i = 0; i < followers.size(); i += chunkSize) {
            int end = Math.min(i + chunkSize, followers.size());
            List<Long> chunk = followers.subList(i, end);
            PostCreatedEvent event = new PostCreatedEvent(p.getId(), p.getUserId(), p.getContent(), p.getCreatedAt());
            for (Long followerId : chunk) {
                rabbit.convertAndSend(AmqpConfig.EXCHANGE_FEED_EVENTS, "user." + followerId, event);
            }
        }
    }
}
