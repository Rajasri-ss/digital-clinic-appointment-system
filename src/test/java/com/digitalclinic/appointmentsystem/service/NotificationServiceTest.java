package com.digitalclinic.appointmentsystem.service;

import com.digitalclinic.appointmentsystem.dto.NotificationDTO;
import com.digitalclinic.appointmentsystem.model.Notification;
import com.digitalclinic.appointmentsystem.model.User;
import com.digitalclinic.appointmentsystem.repository.NotificationRepository;
import com.digitalclinic.appointmentsystem.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void testSendNotification() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Notification mockNotification = Notification.builder()
                .id(100L)
                .user(user)
                .title("Test")
                .message("Message")
                .type("SYSTEM")
                .isRead(false)
                .build();

        when(notificationRepository.save(any(Notification.class))).thenReturn(mockNotification);

        NotificationDTO dto = notificationService.sendNotification(userId, "Test", "Message", "SYSTEM");

        assertNotNull(dto);
        assertEquals("Test", dto.getTitle());
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(messagingTemplate, times(1)).convertAndSendToUser(eq("1"), eq("/queue/notifications"),
                any(NotificationDTO.class));
    }
}
