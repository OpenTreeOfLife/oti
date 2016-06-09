import sys
from check import *
from check_oti import *
status = 0

check_v2_tree = check_blob([field(u'oti_tree_id', check_string),
                            field(u'ot:studyId', check_string),
                            field(u'is_deprecated', check_boolean),  # never true
                            field(u'ot:originalLabel', check_string),  # useless
                            field(u'ot:ottId', check_integer),  # useless
                            field(u'ot:ottTaxonName', check_string),  # useless
                            field(u'ot:branchLengthMode', check_string),
                            field(u'ot:branchLengthDescription', check_string)])

check_v2_study = check_blob(study_fields +
                            [field(u'is_deprecated', check_boolean),
                             field(u'matched_trees', check_list(check_v2_tree))])

status += \
simple_test('/v2/studies/find_trees',
            {'property': 'ot:studyId',
             'value': 'pg_41',
             'verbose': True},
            check_blob([field(u'matched_studies', check_list(check_v2_study))]))

sys.exit(status)
