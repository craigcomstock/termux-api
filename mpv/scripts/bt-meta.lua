local f = os.getenv("TMPDIR") .. "/mpv-meta.txt"
local function send(line)
  local h = io.open(f, "a"); if h then h:write(line .. "\n"); h:close() end
end
local function meta()
  local m = mp.get_property_native("metadata") or {}
  local function g(k) return m[k] or m[k:upper()] or "" end
  local title = g("title"); if title == "" then title = mp.get_property("media-title") or "" end
  local dur = math.floor((mp.get_property_number("duration") or 0) * 1000)
  send(table.concat({"META", title, g("artist"), g("album"), dur}, "\t"))
end
local function state()
  local pos = math.floor((mp.get_property_number("time-pos") or 0) * 1000)
  send(string.format("STATE\t%s\t%d", mp.get_property_native("pause") and "paused" or "playing", pos))
end
mp.observe_property("metadata", "native", meta)
mp.register_event("file-loaded", function() meta(); state() end)
mp.observe_property("pause", "bool", state)
mp.register_event("seek", state)
