FROM node:18

WORKDIR /app

# Copy package.json and package-lock.json
COPY package*.json ./

# Install Angular CLI globally
RUN npm install -g @angular/cli

# Install dependencies
RUN npm install

# Copy project files
COPY . .

EXPOSE 4200

# Serve the application
CMD ["ng", "serve", "--host", "0.0.0.0", "--port", "4200"]
