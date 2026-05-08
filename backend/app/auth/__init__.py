"""
Auth Package — JWT authentication structure (ready for implementation).

This package will contain:
- JWT token generation and validation
- Firebase Auth token verification
- Request dependency for protected endpoints
"""

from __future__ import annotations

from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer

# Security scheme for Swagger UI — expects Bearer token
security_scheme = HTTPBearer(auto_error=False)


async def get_current_user(
    credentials: HTTPAuthorizationCredentials | None = Depends(security_scheme),
) -> dict:
    """
    Dependency that extracts and validates the current user from
    the Authorization header.

    Currently a passthrough stub — will be implemented with either:
    - Firebase Auth ID token verification, or
    - Custom JWT validation

    Usage in routes:
        @router.get("/protected")
        async def protected_route(user: dict = Depends(get_current_user)):
            ...
    """
    # TODO: Implement actual token validation
    # For now, allow unauthenticated access during development
    if credentials is None:
        # In production, raise HTTPException(401)
        return {"uid": "anonymous", "role": "CUSTOMER"}

    token = credentials.credentials

    # Stub: return a mock user dict
    # In production, verify with firebase_admin.auth.verify_id_token(token)
    return {
        "uid": "authenticated-user",
        "token": token,
        "role": "CUSTOMER",
    }


async def require_auth(
    user: dict = Depends(get_current_user),
) -> dict:
    """
    Stricter auth dependency that rejects anonymous users.
    Use this for endpoints that must be authenticated.
    """
    if user.get("uid") == "anonymous":
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Authentication required",
            headers={"WWW-Authenticate": "Bearer"},
        )
    return user
