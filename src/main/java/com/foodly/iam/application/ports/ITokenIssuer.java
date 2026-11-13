
package com.foodly.iam.application.ports;

import com.foodly.iam.domain.model.User;
import java.time.Instant;

public interface ITokenIssuer {

    Token issue(User user);

    record Token(
            String value,
            Instant expiresAt
    ) {
    }
}
