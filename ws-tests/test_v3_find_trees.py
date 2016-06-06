import sys
from check import *
from check_oti import *
status = 0

check_study_plus = check_blob(study_fields +
                              [field(u'matched_trees', check_list(check_tree))])

status += \
simple_test('/v3/studies/find_trees',
            {'property': 'ot:studyId',
             'value': 'pg_41',
             'verbose': True},
            check_blob([field(u'matched_studies', check_list(check_study_plus))]))

sys.exit(status)
