-- V7: 오분이 상점 아이템을 실제 이미지 파일명에 맞게 업데이트

-- 기존 아이템 데이터 삭제
DELETE FROM obooni_items;

-- 실제 존재하는 이미지 파일에 맞춰 아이템 재삽입
INSERT INTO obooni_items (name, price, image_url, item_type) VALUES
('곰돌이 코트', 0, '/images/items/obooni_bear_coat.png', 'CLOTHING'),
('베이지 드레스', 100, '/images/items/obooni_beige_dress.png', 'CLOTHING'),
('생일 수트', 0, '/images/items/obooni_birthday_suit.png', 'CLOTHING'),
('데님 멜빵바지', 200, '/images/items/obooni_denim_overalls.png', 'CLOTHING'),
('초록 셔츠', 150, '/images/items/obooni_green_shirt.png', 'CLOTHING'),
('오렌지 더플백', 180, '/images/items/obooni_orange_duffle.png', 'CLOTHING'),
('핑크 드레스', 250, '/images/items/obooni_pink_dress.png', 'CLOTHING'),
('빨간 후드티', 220, '/images/items/obooni_red_hoodie.png', 'CLOTHING'),
('하늘 곰돌이', 300, '/images/items/obooni_sky_bear.png', 'CLOTHING'),
('노란 티셔츠', 120, '/images/items/obooni_yellow_tshirt.png', 'CLOTHING');
