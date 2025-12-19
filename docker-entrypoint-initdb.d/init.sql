IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'jacketshop')
BEGIN
    CREATE DATABASE jacketshop;
    PRINT '✅ Database jacketshop created successfully!';
END
ELSE
BEGIN
    PRINT 'ℹ️ Database jacketshop already exists.';
END
