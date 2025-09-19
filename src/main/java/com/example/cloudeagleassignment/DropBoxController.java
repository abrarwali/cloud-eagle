package com.example.cloudeagleassignment;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxTeamClientV2;
import com.dropbox.core.v2.team.TeamMemberInfo;
import com.dropbox.core.v2.team.TeamMemberProfile;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cloudeagle/dropbox")
public class DropBoxController {
//    private static final String SCOPES = "members.read events.read team_info.read account_info.read";
    private static final String SCOPES = "";
    private final DropboxConfig dropboxConfig;

    public DropBoxController(DropboxConfig dropboxConfig) {
        this.dropboxConfig = dropboxConfig;
    }

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Dropbox Test Endpoint");
    }


    @GetMapping("/members")
    public ResponseEntity<?> getTeamMembers(@RequestHeader("Authorization") String authorizationHeader) throws Exception {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Bearer token");
        }

        String accessToken = extractAccessToken(authorizationHeader);
        DbxTeamClientV2 teamClient = createTeamClient(accessToken);

        List<TeamMemberInfo> teamMembers = teamClient.team().membersList().getMembers();
        List<MemberDTO> memberDtos = convertToMemberDtos(teamMembers);

        return ResponseEntity.ok(memberDtos);
    }

    @GetMapping("/authorize")
    public void authorize(HttpServletResponse response) throws IOException {
        System.out.println("Redirect URI: " + dropboxConfig.getRedirectUri());
        String url = "https://www.dropbox.com/oauth2/authorize"
                + "?client_id=" + dropboxConfig.getClientId()
                + "&redirect_uri=" + dropboxConfig.getRedirectUri()
                + "&response_type=code"
                + "&token_access_type=offline"
                + "&scope=" + SCOPES;
        response.sendRedirect(url);
    }

    @GetMapping("/callback")
    public ResponseEntity<String> callback(@RequestParam("code") String code) throws IOException {
        OkHttpClient client = new OkHttpClient();

        FormBody body = new FormBody.Builder()
                .add("code", code)
                .add("grant_type", "authorization_code")
                .add("client_id", dropboxConfig.getClientId())
                .add("client_secret", dropboxConfig.getClientSecret())
                .add("redirect_uri", dropboxConfig.getRedirectUri())
                .build();

        Request request = new Request.Builder()
                .url("https://api.dropboxapi.com/oauth2/token")
                .post(body)
                .build();


        try (Response response = client.newCall(request).execute()) {
            return ResponseEntity.ok(response.body().string());
        }
    }
    private String extractAccessToken(String authorizationHeader) {
        return authorizationHeader.substring(7);
    }

    private DbxTeamClientV2 createTeamClient(String accessToken) {
        DbxRequestConfig config = DbxRequestConfig.newBuilder("my-app/1.0").build();
        return new DbxTeamClientV2(config, accessToken);
    }

    private List<MemberDTO> convertToMemberDtos(List<TeamMemberInfo> teamMembers) {
        return teamMembers.stream()
                .map(this::convertToMemberDto)
                .collect(Collectors.toList());
    }

    private MemberDTO convertToMemberDto(TeamMemberInfo teamMember) {
        TeamMemberProfile profile = teamMember.getProfile();
        return new MemberDTO(
                profile.getTeamMemberId(),
                profile.getEmail(),
                profile.getName().getDisplayName()
        );
    }
}