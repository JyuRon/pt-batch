.PHONY: docker-up docker-down

# docker-compose [up]컨테이너 생성 이후 실행, [d]백그라운드, [force--recreate]컨테이너를 지우고 생성
# 콘솔창 : make docker-up
docker-up:
	docker-compose up -d --force-recreate

# docker-compose [down]컨테이너 정지 이후 삭제, [v]볼륨까지 삭제
docker-down:
	docker-compose down -v