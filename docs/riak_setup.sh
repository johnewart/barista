riak-admin bucket-type create maps '{"props":{"datatype":"map"}}'
riak-admin bucket-type create sets '{"props":{"datatype":"set"}}'
riak-admin bucket-type create cookbooks
riak-admin bucket-type activate cookbooks
riak-admin bucket-type update cookbooks '{"props":{"allow_mult":false}}'
riak-admin bucket-type activate sets
riak-admin bucket-type activate maps
