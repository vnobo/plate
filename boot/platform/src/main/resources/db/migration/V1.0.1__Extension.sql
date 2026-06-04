-- create the extension
create extension if not exists zhparser;

-- make test configuration using parser
drop text search configuration if exists chinese cascade;
create text search configuration chinese (parser = zhparser);

-- add token mapping
alter text search configuration chinese add mapping for n,v,a,i,e,l,x with simple;
-- set default text search configuration is chinese
set default_text_search_config = 'chinese';
-- test default text search configuration
show default_text_search_config;

-- truncate custom word table
truncate table zhparser.zhprs_custom_word;
--add custom word
insert into zhparser.zhprs_custom_word values ('支付宝'), ('资金压力');
--sync custom word
select sync_zhprs_custom_word();

--test zhparser extension is ok

select ts_token_type('zhparser');
select ts_debug('chinese', '支付宝使用很方便');
select ts_debug('chinese', '保障房资金压力');

-- test custom word
select ts_parse('zhparser', '支付宝使用很方便');
select ts_parse('zhparser', '保障房资金压力');

-- test to_tsvector
select to_tsvector('chinese', '支付宝使用很方便');
select to_tsquery('chinese', '支付宝使用很方便');
select to_tsvector('chinese', '保障房资金压力');
select to_tsquery('chinese', '保障房资金压力');