dim = size (time);
hold on;
acum=0;
count=0;
pontos_time = 0:100:900 ;
pontos_delay = zeros(1,10);
pontos_delay_count = zeros(1,10);

for i=1:dim(2)
   
    
    
    if time(i)>100 && time(i)<101
        pontos_delay(2)=pontos_delay(2)+delay(i);
        pontos_delay_count(2) = pontos_delay_count(2)+1;
    end
    
    if time(i)>200 && time(i)<201
       pontos_delay(3)=pontos_delay(3)+delay(i);
        pontos_delay_count(3) = pontos_delay_count(3)+1;
    end
    if time(i)>300 && time(i)<301
        pontos_delay(4)=pontos_delay(4)+delay(i);
        pontos_delay_count(4) = pontos_delay_count(4)+1;
    end
    if time(i)>400 && time(i)<401
       pontos_delay(5)=pontos_delay(5)+delay(i);
        pontos_delay_count(5) = pontos_delay_count(5)+1;
    end
    if time(i)>500 && time(i)<501
        pontos_delay(6)=pontos_delay(6)+delay(i);
        pontos_delay_count(6) = pontos_delay_count(6)+1;
    end    
    if time(i)>600 && time(i)<601
       pontos_delay(7)=pontos_delay(7)+delay(i);
        pontos_delay_count(7) = pontos_delay_count(7)+1;
    end
    if time(i)>700 && time(i)<701
        pontos_delay(8)=pontos_delay(8)+delay(i);
        pontos_delay_count(8) = pontos_delay_count(8)+1;
    end
    if time(i)>800 && time(i)<801
        pontos_delay(9)=pontos_delay(9)+delay(i);
        pontos_delay_count(9) = pontos_delay_count(9)+1;
    end
    if time(i)>900 && time(i)<901
        pontos_delay(10)=pontos_delay(10)+delay(i);
        pontos_delay_count(10) = pontos_delay_count(10)+1;
    end
   
end

plot(pontos_time, pontos_delay./pontos_delay_count,'-.*c');