from check import *

study_fields = [field(u'ot:studyPublicationReference', check_string),
                field(u'ot:curatorName', check_string),
                field(u'ot:studyId', check_string),
                field(u'ot:focalClade', check_integer),
                field(u'ot:focalCladeOTTTaxonName', check_string),
                field(u'ot:dataDeposit', check_string),
                field(u'ot:studyPublication', check_string),
                field(u'ot:candidateTreeForSynthesis', check_string),
                field(u'ot:studyYear', check_integer)]

check_study = check_blob(study_fields)

tree_fields = [field(u'oti_tree_id', check_string),
               field(u'ot:studyId', check_string),
               field(u'ot:branchLengthMode', check_string),
               field(u'ot:branchLengthDescription', check_string)]

check_tree = check_blob(tree_fields)

