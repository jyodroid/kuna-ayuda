-- Optional, opt-in contact on mutual-aid board posts (#3). A poster may share a phone and/or an email
-- so helpers can reach them — but it's optional now (previously contact_phone was mandatory), the create
-- UI shows a "this will be public" disclaimer, and closing a post clears the contact fields (the contact
-- data is deleted when the post is taken down).
ALTER TABLE resource_posts ALTER COLUMN contact_phone DROP NOT NULL;
ALTER TABLE resource_posts ADD COLUMN contact_email VARCHAR(160);
