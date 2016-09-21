
# tests the otindex implementation of the v3 properties method
# against oti
import sys, os
from opentreetesting import test_http_json_method, config

# DOMAIN_OTINDEX = config('host', 'apihost')
# CONTROLLER = DOMAIN_OTINDEX + '/v3/studies'
# SUBMIT_URI = CONTROLLER + '/properties'
# otindex_result = test_http_json_method(SUBMIT_URI,
#                           'POST',
#                           expected_status=200,
#                           return_bool_data=True)
# assert otindex_result[0] is True
# k = otindex_result[1].keys()
# print otindex_result[1]
# assert 'study_properties' in k
# assert isinstance(otindex_result[1]['tree_properties'], list)

DOMAIN_OTI = config('host', 'otihost')
CONTROLLER = DOMAIN_OTI + '/v3/studies'
SUBMIT_URI = CONTROLLER + '/properties'
oti_result = test_http_json_method(SUBMIT_URI,
                          'POST',
                          expected_status=200,
                          return_bool_data=True)
assert oti_result[0] is True
k = oti_result[1].keys()
print oti_result[1]
assert 'study_properties' in k
assert isinstance(oti_result[1]['tree_properties'], list)

# properties lists should be identical
assert otindex_result == oti_result
