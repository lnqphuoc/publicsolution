package com.app.server.utils;

import com.app.server.config.ConfigInfo;
import com.app.server.data.SessionData;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.JSONUtil;
import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;

import javax.xml.bind.DatatypeConverter;
import java.util.Date;

/*
    Our simple static class that demonstrates how to create and decode JWTs.
 */
@Component
public class JwtUtil {

    // The secret key. This should be in a property file NOT under source
    // control and not hard coded in real life. We're putting it here for
    // simplicity.
    private String key = "session";

    //Sample method to construct a JWT
    public String createJWT(SessionData sessionData, long millisecond) {

        Date now = new Date();
        Claims claims = Jwts.claims().setSubject(sessionData.getName());
        claims.put(key, JSONUtil.Serialize(sessionData));
        JwtBuilder builder = Jwts.builder()
                .setIssuedAt(now)
                .setHeaderParam("type", "JWT")
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS256, ConfigInfo.JWT_SECRET_KEY);
        // If millisecond has been specified, let's add the expiration
        if (millisecond > 0) {
            long expMillis = now.getTime() + millisecond;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }
        // Builds the JWT and serializes it to a compact, URL-safe string
        String token = builder.compact();
        System.out.println(JSONUtil.Serialize(sessionData));
        System.out.println(token);
        return token;
    }

    public Claims decodeJWT(String jwt) {

        //This line will throw an exception if it is not a signed JWS (as expected)
        Claims claims = Jwts.parser()
                .setSigningKey(DatatypeConverter.parseBase64Binary(ConfigInfo.JWT_SECRET_KEY))
                .parseClaimsJws(jwt).getBody();
        return claims;
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(ConfigInfo.JWT_SECRET_KEY).parseClaimsJws(token);
//            if (claims.getBody().getExpiration().before(new Date())) {
//                return false;
//            }
            return true;
        } catch (JwtException | IllegalArgumentException e) {

        }
        return false;
    }

    // get data from jwt token
    public SessionData getData(String token) {
        try {
            Claims claim = decodeJWT(token);
            if (claim != null)
                return JSONUtil.DeSerialize(claim.get(key).toString(), SessionData.class);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return null;
    }

    public String createJWTLink(String data) {

        Date now = new Date();
        Claims claims = Jwts.claims().setSubject("export");
        claims.put("data", data);
        JwtBuilder builder = Jwts.builder()
                .setIssuedAt(now)
                .setHeaderParam("type", "JWT")
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS256, ConfigInfo.JWT_SECRET_KEY);
        // If millisecond has been specified, let's add the expiration
        long expMillis = now.getTime() + 100000;
        Date exp = new Date(expMillis);
        builder.setExpiration(exp);
        // Builds the JWT and serializes it to a compact, URL-safe string
        String jwt = builder.compact();
        return jwt;
    }
}