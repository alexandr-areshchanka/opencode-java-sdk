# OpenCode Server Dockerfile with Z.AI Provider Configuration
FROM node:24-alpine

# OpenCode version to install (override with --build-arg OPENCODE_VERSION=x.y.z)
ARG OPENCODE_VERSION

# Install required packages
RUN apk add --no-cache curl gettext bash

# Set working directory
WORKDIR /app

# Install OpenCode using the official install script with pinned version
RUN curl -fsSL https://opencode.ai/install | bash -s -- --version ${OPENCODE_VERSION}

# Verify installed version matches the requested version
RUN INSTALLED_VERSION=$(/root/.opencode/bin/opencode --version 2>/dev/null | grep -oE '[0-9]+\.[0-9]+\.[0-9]+' | head -1) && \
    if [ "$INSTALLED_VERSION" != "${OPENCODE_VERSION}" ]; then \
        echo "ERROR: Version mismatch - expected ${OPENCODE_VERSION}, got ${INSTALLED_VERSION}"; \
        exit 1; \
    fi && \
    echo "OpenCode ${INSTALLED_VERSION} installed successfully"

# Add OpenCode to PATH
ENV PATH="/root/.opencode/bin:$PATH"

# Create configuration directories
RUN mkdir -p /app/config ~/.config/opencode ~/.local/share/opencode

# Copy configuration files and startup script
COPY config/ /app/config/
COPY start.sh /app/start.sh

# Make startup script executable and convert line endings
RUN chmod +x /app/start.sh && \
    sed -i 's/\r$//' /app/start.sh

# Set environment variables
ENV OPENCODE_SERVER_PORT=4096
ENV OPENCODE_SERVER_HOST=0.0.0.0

# Expose port
EXPOSE 4096

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:4096/global/health || exit 1

# Start OpenCode server using startup script
CMD ["/app/start.sh"]