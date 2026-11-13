
package com.foodly.iam.application.handlers;

import com.foodly.iam.domain.model.User;
import com.foodly.iam.application.ports.ITokenIssuer;

public record AuthenticationResult(
        User user,
        ITokenIssuer.Token token
) {
}
