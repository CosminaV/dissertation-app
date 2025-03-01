package ro.ase.ism.dissertation.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ro.ase.ism.dissertation.model.token.RefreshToken;
import ro.ase.ism.dissertation.model.user.User;
import ro.ase.ism.dissertation.repository.RefreshTokenRepository;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
@Service
public class JwtService {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpire;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpire;

    private final RefreshTokenRepository refreshTokenRepository;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateAccessToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails) {
        int tokenVersion = ((User) userDetails).getTokenVersion();
        extraClaims.put("token_version", tokenVersion);
        return generateToken(extraClaims, userDetails, accessTokenExpire);
    }

    public String generateRefreshToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails) {
        return generateToken(extraClaims, userDetails, refreshTokenExpire);
    }

    public boolean isAccessTokenValid(String token, UserDetails userDetails) {
        final Claims claims = extractAllClaims(token);
        Integer tokenVersion = claims.get("token_version", Integer.class);
        int currentTokenVersion = ((User) userDetails).getTokenVersion();

        return tokenVersion.equals(currentTokenVersion) && isTokenValid(token, userDetails);
    }

    public boolean isRefreshTokenValid(String token, UserDetails userDetails) {
        boolean isTokenNotRevoked = refreshTokenRepository.findByToken(token)
                .map(rt -> !rt.isLoggedOut()).orElse(false);

        return isTokenNotRevoked && isTokenValid(token, userDetails);
    }

    public void revokeAllRefreshTokens(User user) {
        List<RefreshToken> validRefreshTokens =
                refreshTokenRepository.findAllRefreshTokenByUser(user.getId());
        if(!validRefreshTokens.isEmpty()) {
            validRefreshTokens.forEach(rt -> rt.setLoggedOut(true));
        }

        refreshTokenRepository.saveAll(validRefreshTokens);
    }

    public void incrementAccessTokenVersion(User user) {
        user.setTokenVersion(user.getTokenVersion() + 1);
    }

    private String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expireTime) {
        long now = System.currentTimeMillis();
        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(now))
                .expiration(new Date(now + expireTime))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
//        return Jwts.parser()
//                .setSigningKey(getSignInKey()) // to create the signature
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
