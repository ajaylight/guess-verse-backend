-- Logo images are now resolved through Logo.dev.
-- image_domain remains server-side and is never returned in the question DTO.

ALTER TABLE logo_questions
    RENAME COLUMN image_name TO image_domain;

UPDATE logo_questions
SET image_domain = CASE LOWER(image_domain)
    WHEN 'nike.png' THEN 'nike.com'
    WHEN 'tesla.png' THEN 'tesla.com'
    WHEN 'spotify.png' THEN 'spotify.com'
    WHEN 'microsoft.png' THEN 'microsoft.com'
    WHEN 'apple.png' THEN 'apple.com'
    WHEN 'adidas.png' THEN 'adidas.com'
    WHEN 'amazon.png' THEN 'amazon.com'
    WHEN 'google.png' THEN 'google.com'
    WHEN 'coca_cola.png' THEN 'coca-cola.com'
    WHEN 'mcdonalds.png' THEN 'mcdonalds.com'
    ELSE image_domain
END;

INSERT INTO logo_questions (image_domain, answer, difficulty, active)
SELECT v.image_domain, v.answer, 'MEDIUM', TRUE
FROM (VALUES
    ('bmw.com', 'BMW'),
    ('netflix.com', 'NETFLIX'),
    ('puma.com', 'PUMA'),
    ('starbucks.com', 'STARBUCKS'),
    ('uber.com', 'UBER'),
    ('airbnb.com', 'AIRBNB'),
    ('paypal.com', 'PAYPAL'),
    ('pepsi.com', 'PEPSI'),
    ('linkedin.com', 'LINKEDIN'),
    ('disney.com', 'DISNEY')
) AS v(image_domain, answer)
WHERE NOT EXISTS (
    SELECT 1
    FROM logo_questions q
    WHERE UPPER(q.answer) = UPPER(v.answer)
);

INSERT INTO logo_questions (image_domain, answer, difficulty, active)
SELECT v.image_domain, v.answer, 'HARD', TRUE
FROM (VALUES
    ('adobe.com', 'ADOBE'),
    ('cisco.com', 'CISCO'),
    ('dell.com', 'DELL'),
    ('intel.com', 'INTEL'),
    ('hp.com', 'HP'),
    ('mastercard.com', 'MASTERCARD'),
    ('visa.com', 'VISA'),
    ('ebay.com', 'EBAY'),
    ('pinterest.com', 'PINTEREST'),
    ('snapchat.com', 'SNAPCHAT')
) AS v(image_domain, answer)
WHERE NOT EXISTS (
    SELECT 1
    FROM logo_questions q
    WHERE UPPER(q.answer) = UPPER(v.answer)
);