import sys
from check import *
from check_oti import *
status = 0

status += \
simple_test('/v3/studies/find_studies',
            {'property': 'ot:studyId',
             'value': 'pg_41',
             'verbose': True},
            check_blob([field(u'matched_studies', check_list(check_study))]))

sys.exit(status)
