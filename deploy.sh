#!/bin/bash

# Script triển khai ứng dụng ReviewSystem
# Tác giả: Doraemon

# Màu sắc cho output
GREEN="\033[0;32m"
YELLOW="\033[0;33m"
NC="\033[0m" # No Color

echo -e "${YELLOW}Bắt đầu quá trình triển khai...${NC}"

# Đường dẫn đến thư mục dự án
PROJECT_DIR="/Users/macbook/Project/JavaProject/ReviewSystem"

# Đường dẫn đến thư mục Tomcat
TOMCAT_DIR="/opt/homebrew/Cellar/tomcat/11.0.6/libexec"

# Di chuyển đến thư mục dự án
cd "$PROJECT_DIR"

# Biên dịch dự án với Maven
echo -e "${YELLOW}Đang biên dịch dự án...${NC}"
mvn clean package

if [ $? -ne 0 ]; then
    echo -e "\033[0;31mLỗi biên dịch! Quá trình triển khai bị hủy.${NC}"
    exit 1
fi

# Sao chép file WAR đến thư mục webapps của Tomcat
echo -e "${YELLOW}Đang sao chép file WAR...${NC}"
cp "$PROJECT_DIR/target/ReviewSystem.war" "$TOMCAT_DIR/webapps/ROOT.war"

if [ $? -ne 0 ]; then
    echo -e "\033[0;31mLỗi sao chép file WAR! Quá trình triển khai bị hủy.${NC}"
    exit 1
fi

# Khởi động lại Tomcat
echo -e "${YELLOW}Đang khởi động lại Tomcat...${NC}"
brew services restart tomcat

if [ $? -ne 0 ]; then
    echo -e "\033[0;31mLỗi khởi động lại Tomcat! Vui lòng kiểm tra lại.${NC}"
    exit 1
fi

# Hoàn thành
echo -e "${GREEN}Triển khai thành công!${NC}"
echo -e "${GREEN}Ứng dụng đã được triển khai tại: http://localhost:8080${NC}"

# Hiển thị log Tomcat (tùy chọn)
echo -e "${YELLOW}Bạn có muốn xem log của Tomcat không? (y/n)${NC}"
read -r answer

if [ "$answer" = "y" ] || [ "$answer" = "Y" ]; then
    tail -f "$TOMCAT_DIR/logs/catalina.$(date +%Y-%m-%d).log"
fi
