def ctx = 'stringtosearch'

def regexMap = 
[v_A_ind:/(.*)Aevent(.*)/
, v_B_ind:/(.*)uBevent(.*)/
, v_C_ind:/(.*)uCevent(.*)/,
, v_D_ind:/(.*)uDevent(.*)/
]

def result = [:] as Map<String,String>

for (entry in regexMap) {
matcher = ( ctx=~ entry.value.toString() )

    if (matcher.matches()) {
        result.put(entry.key,1)
    } else result.put(entry.key,0)

}

println(result)

def json = groovy.json.JsonOutput.toJson(result)
print(json)
