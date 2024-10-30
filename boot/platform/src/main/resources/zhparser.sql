-- create the extension
create extension zhparser;

-- make test configuration using parser
create text search configuration chinese (parser = zhparser);

-- add token mapping
alter text search configuration chinese add mapping for n,v,a,i,e,l,x with simple;
set default_text_search_config = 'chinese';
show default_text_search_config;
--add custom word
insert into zhparser.zhprs_custom_word
values ('支付宝');
insert into zhparser.zhprs_custom_word
values ('资金压力');
select sync_zhprs_custom_word();
alter extension zhparser update;
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