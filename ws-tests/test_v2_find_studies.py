#!/usr/bin/env python
from opentreetesting import test_http_json_method, config
import json
import sys

DOMAIN = config('host', 'apihost')
SUBMIT_URI = DOMAIN + '/v2/studies/find_studies'
data = {'property': 'ot:studyId',
        'value': 'pg_41',
        'verbose': True}
r = test_http_json_method(SUBMIT_URI,
                          'POST',
                          data=data,
                          expected_status=200,
                          return_bool_data=True)
if not r[0]:
    sys.exit(1)
resp = r[1]
study = resp['matched_studies'][0]
if study['ot:focalCladeOTTTaxonName'] != 'Feddea':
    sys.exit(1)

