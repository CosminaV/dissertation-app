package ro.ase.ism.dissertation.controller.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ro.ase.ism.dissertation.model.user.User;
import ro.ase.ism.dissertation.service.FileStorageService;
import ro.ase.ism.dissertation.service.UserService;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final FileStorageService fileStorageService;

    @PostMapping("/profile-image")
    public ResponseEntity<?> uploadProfileImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user
    ) {
        userService.uploadFaceImage(user.getId(), file);
        return ResponseEntity.ok("Profile image uploaded successfully.");
    }

    @GetMapping("/profile-image-url")
    public ResponseEntity<String> getPresignedProfileImageUrl(@AuthenticationPrincipal User user) {
        log.info("Entered controller: profile-image-url");
        if (user.getFaceImagePath() == null) {
            return ResponseEntity.badRequest().body("User has no profile image");
        }

        String url = fileStorageService.generatePresignedUrl(user.getFaceImagePath(), 600); // valid for 10 minutes
        return ResponseEntity.ok(url);
    }
}
