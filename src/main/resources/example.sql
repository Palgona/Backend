-- Member 테이블에 예시값 삽입
INSERT INTO member (nick_name, mileage, social_id, profile_image, status, role)
VALUES
    ('user1', 100, '1234567890', 'profile1.jpg', 'ACTIVE', 'USER'),
    ('user2', 150, '0987654321', 'profile2.jpg', 'ACTIVE', 'USER');

-- Product 테이블에 예시값 삽입
INSERT INTO product (name, initial_price, content, category, deadline, member_id)
VALUES
    ('Product 1', 1000, 'Content for Product 1', 'DIGITAL_DEVICE',(DATE_ADD(NOW(), INTERVAL 1 MINUTE)), 1),
    ('Product 2', 2000, 'Content for Product 2', 'CLOTHING', (DATE_ADD(NOW(), INTERVAL 1 MINUTE)), 2);

-- Bidding 테이블에 예시값 삽입
INSERT INTO bidding (product_id, member_id, price, state)
VALUES
    (1, 1, 1200, 'ATTEMPT'),
    (1, 2, 1500, 'ATTEMPT'),
    (2, 1, 2200, 'ATTEMPT'),
    (2, 2, 1800, 'ATTEMPT');
