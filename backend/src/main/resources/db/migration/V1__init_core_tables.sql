create table players (
    id bigserial primary key,
    tekken_id varchar(32) not null unique,
    name varchar(255),
    platform varchar(64),
    platform_id varchar(128),
    language varchar(32),
    region varchar(64),
    tekken_prowess integer,
    main_character_json jsonb,
    last_seen timestamptz,
    raw_profile_json jsonb,
    fetched_at timestamptz not null default now(),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table matches (
    id bigserial primary key,
    external_match_key varchar(128) not null unique,
    battle_at timestamptz not null,
    battle_type varchar(64),
    game_version integer,
    stage_id integer,
    winner integer,
    p1_tekken_id varchar(32),
    p1_name varchar(255),
    p1_char varchar(64),
    p1_region varchar(64),
    p1_dan_rank varchar(128),
    p1_tekken_power integer,
    p1_rounds_won integer,
    p2_tekken_id varchar(32),
    p2_name varchar(255),
    p2_char varchar(64),
    p2_region varchar(64),
    p2_dan_rank varchar(128),
    p2_tekken_power integer,
    p2_rounds_won integer,
    raw_battle_json jsonb,
    fetched_at timestamptz not null default now(),
    created_at timestamptz not null default now()
);

create index idx_matches_p1_tekken_id_battle_at on matches (p1_tekken_id, battle_at desc);
create index idx_matches_p2_tekken_id_battle_at on matches (p2_tekken_id, battle_at desc);

create table api_cache (
    cache_key varchar(255) primary key,
    source varchar(64) not null,
    response_json jsonb not null,
    expires_at timestamptz not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index idx_api_cache_expires_at on api_cache (expires_at);

create table player_search_history (
    id bigserial primary key,
    query varchar(255) not null,
    tekken_id varchar(32),
    searched_at timestamptz not null default now()
);

create index idx_player_search_history_searched_at on player_search_history (searched_at desc);
