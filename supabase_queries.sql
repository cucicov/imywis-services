---- create a function and trigger for user signup to create their default handles
-- FUNCTION
create or replace function public.handle_user_confirmed()
returns trigger as $$
declare
user_handle text;
begin
  -- Extract part before @ from email
  user_handle := split_part(new.email, '@', 1);

  -- Insert into your table
insert into public.user_profiles (user_id, handle)
values (new.id, user_handle);

return new;
end;
$$ language plpgsql;

-- TRIGGER
create trigger on_user_confirmed
    after update on auth.users
    for each row
    when (
        old.email_confirmed_at is null
            and new.email_confirmed_at is not null
        )
    execute function public.handle_user_confirmed();

-- GRANT RIGHTS TO SUPABASE AUTH ADMIN
grant insert on table public.user_profiles to supabase_auth_admin;

-- ADAPT FUNCTION AND TRIGGER TO BYPASS RLS
create or replace function public.handle_user_confirmed()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
user_handle text;
begin
  -- Extract part before @ from email
  user_handle := split_part(new.email, '@', 1);

  -- Insert into your table
insert into public.user_profiles (user_id, handle)
values (new.id, user_handle);

return new;
end;
$$;
--
alter function public.handle_user_confirmed() owner to postgres;

-- EXTEND TABLE TO HOLD USER PROJECT
alter table public.user_profiles
    add column data jsonb;