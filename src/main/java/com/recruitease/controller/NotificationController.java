package com.recruitease.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recruitease.entity.Notification;
import com.recruitease.repository.NotificationRepository;

@RestController
@RequestMapping("/api/student-notifications")
@CrossOrigin("*")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /* ==========================
       GET ALL NOTIFICATIONS
       ========================== */

    @GetMapping("/student/{studentId}")
    public List<Notification> getStudentNotifications(
            @PathVariable Long studentId) {

        return notificationRepository
                .findByStudentIdOrderByCreatedAtDesc(studentId);
    }

    /* ==========================
       CREATE NOTIFICATION
       ========================== */

    @PostMapping
    public Notification createNotification(
            @RequestBody Notification notification) {

        notification.setReadStatus(false);

        return notificationRepository.save(notification);
    }

    /* ==========================
       MARK SINGLE READ
       ========================== */

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markRead(
            @PathVariable Long id) {

        Notification notification =
                notificationRepository.findById(id).orElse(null);

        if (notification == null) {
            return ResponseEntity.notFound().build();
        }

        notification.setReadStatus(true);

        notificationRepository.save(notification);

        return ResponseEntity.ok(notification);
    }

    /* ==========================
       MARK ALL READ
       ========================== */

    @PutMapping("/student/{studentId}/read-all")
    public ResponseEntity<?> markAllRead(
            @PathVariable Long studentId) {

        List<Notification> list =
                notificationRepository
                        .findByStudentIdOrderByCreatedAtDesc(studentId);

        for (Notification n : list) {

            n.setReadStatus(true);

        }

        notificationRepository.saveAll(list);

        return ResponseEntity.ok("All notifications marked as read.");

    }

    /* ==========================
       DELETE NOTIFICATION
       ========================== */

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(
            @PathVariable Long id) {

        if (!notificationRepository.existsById(id)) {

            return ResponseEntity.notFound().build();

        }

        notificationRepository.deleteById(id);

        return ResponseEntity.ok("Notification deleted.");

    }
    @GetMapping("/demo/{studentId}")
public Notification demo(@PathVariable Long studentId){

    Notification n = new Notification();

    n.setStudentId(studentId);

    n.setTitle("Welcome to RecruitEase");

    n.setMessage("Your notification system is working successfully.");

    n.setType("SYSTEM");

    n.setReadStatus(false);

    return notificationRepository.save(n);

}

}